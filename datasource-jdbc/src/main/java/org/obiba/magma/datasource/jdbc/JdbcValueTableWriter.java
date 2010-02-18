/**
 * 
 */
package org.obiba.magma.datasource.jdbc;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import liquibase.change.AddColumnChange;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.InsertDataChange;
import liquibase.change.ModifyColumnChange;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.jdbc.support.CreateTableChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.InsertDataChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.NameConverter;

public class JdbcValueTableWriter implements ValueTableWriter {
  //
  // Constants
  //

  static final String VARIABLE_METADATA_TABLE = "variables";

  static final String ATTRIBUTE_METADATA_TABLE = "variable_attributes";

  static final String CATEGORY_METADATA_TABLE = "categories";

  static final String ENTITY_ID_COLUMN = "entity_id";

  //
  // Instance Variables
  //

  private final JdbcValueTable valueTable;

  //
  // Constructors
  //

  JdbcValueTableWriter(JdbcValueTable valueTable) {
    super();
    this.valueTable = valueTable;
  }

  //
  // ValueTableWriter Methods
  //

  @Override
  public ValueSetWriter writeValueSet(VariableEntity entity) {
    return new JdbcValueSetWriter(entity);
  }

  @Override
  public VariableWriter writeVariables() {
    if(valueTable.getDatasource().getSettings().useMetadataTables()) {
      return new JdbcMetadataVariableWriter();
    } else {
      return new JdbcVariableWriter();
    }
  }

  @Override
  public void close() throws IOException {
    try {
      valueTable.getDatasource().getDatabase().commit();
    } catch(JDBCException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  //
  // Inner Classes
  //

  private class JdbcVariableWriter implements VariableWriter {
    //
    // Instance Variables
    //

    protected List<Change> changes = new ArrayList<Change>();

    //
    // VariableWriter Methods
    //

    @Override
    public void writeVariable(Variable variable) {
      if(!valueTable.isForEntityType(variable.getEntityType())) {
        throw new InvalidParameterException("Wrong entity type for variable '" + variable.getName() + "': " + valueTable.getEntityType() + " expected, " + variable.getEntityType() + " received.");
      }

      doWriteVariable(variable);

      valueTable.addVariableValueSource(new JdbcValueTable.JdbcVariableValueSource(variable));
    }

    @Override
    public void close() throws IOException {
      try {
        List<SqlVisitor> sqlVisitors = Collections.emptyList();
        for(Change change : changes) {
          change.executeStatements(valueTable.getDatasource().getDatabase(), sqlVisitors);
        }
      } catch(JDBCException ex) {
        throw new MagmaRuntimeException(ex);
      } catch(UnsupportedChangeException ex) {
        throw new MagmaRuntimeException(ex);
      }
    }

    //
    // Methods
    //

    protected void doWriteVariable(Variable variable) {
      String columnName = NameConverter.toSqlName(variable.getName());
      ColumnConfig column = new ColumnConfig();
      column.setName(columnName);
      column.setType(SqlTypes.sqlTypeFor(variable.getValueType()));

      if(valueTable.getDatasource().getDatabaseSnapshot().getColumn(valueTable.getSqlName(), columnName) != null) {
        ModifyColumnChange modifyColumnChange = new ModifyColumnChange();
        modifyColumnChange.setTableName(valueTable.getSqlName());
        modifyColumnChange.addColumn(column);
        changes.add(modifyColumnChange);
      } else {
        AddColumnChange addColumnChange = new AddColumnChange();
        addColumnChange.setTableName(valueTable.getSqlName());
        addColumnChange.addColumn(column);
        changes.add(addColumnChange);
      }
    }
  }

  private class JdbcMetadataVariableWriter extends JdbcVariableWriter {
    //
    // Constructors
    //

    JdbcMetadataVariableWriter() {
      if(valueTable.getDatasource().getSettings().useMetadataTables()) {
        createMetadataTablesIfNotPresent();
      }
    }

    //
    // JdbcVariableWriter Methods
    //

    @Override
    protected void doWriteVariable(Variable variable) {
      InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
      builder.tableName(VARIABLE_METADATA_TABLE).withColumn("value_table", valueTable.getSqlName()).withColumn("name", NameConverter.toSqlName(variable.getName())).withColumn("value_type", variable.getValueType().getName()).withColumn("mime_type", variable.getMimeType()).withColumn("units", variable.getUnit()).withColumn("is_repeatable", variable.isRepeatable()).withColumn("occurrence_group", variable.getOccurrenceGroup(), true);
      changes.add(builder.build());

      if(variable.hasAttributes()) {
        System.out.println("Writing attributes");
        writeAttributes(variable);
      }
      if(variable.hasCategories()) {
        System.out.println("Writing categories");
        writeCategories(variable);
      }

      super.doWriteVariable(variable);
    }

    //
    // Methods
    //

    private void createMetadataTablesIfNotPresent() {
      if(valueTable.getDatasource().getDatabaseSnapshot().getTable(VARIABLE_METADATA_TABLE) == null) {
        CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
        builder.tableName(VARIABLE_METADATA_TABLE).withPrimaryKeyColumn("value_table", "VARCHAR(255)").withPrimaryKeyColumn("name", "VARCHAR(255)").withColumn("value_type", "VARCHAR(255)").withNullableColumn("mime_type", "VARCHAR(255)").withNullableColumn("units", "VARCHAR(255)").withColumn("is_repeatable", "BOOLEAN").withNullableColumn("occurrence_group", "VARCHAR(255)");
        changes.add(builder.build());
      }
      if(valueTable.getDatasource().getDatabaseSnapshot().getTable(ATTRIBUTE_METADATA_TABLE) == null) {
        CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
        builder.tableName(ATTRIBUTE_METADATA_TABLE).withPrimaryKeyColumn("value_table", "VARCHAR(255)").withPrimaryKeyColumn("variable_name", "VARCHAR(255)").withPrimaryKeyColumn("attribute_name", "VARCHAR(255)").withColumn("attribute_value", "VARCHAR(255)");
        changes.add(builder.build());
      }
      if(valueTable.getDatasource().getDatabaseSnapshot().getTable(CATEGORY_METADATA_TABLE) == null) {
        CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
        builder.tableName(CATEGORY_METADATA_TABLE).withPrimaryKeyColumn("value_table", "VARCHAR(255)").withPrimaryKeyColumn("variable_name", "VARCHAR(255)").withPrimaryKeyColumn("name", "VARCHAR(255)").withNullableColumn("code", "VARCHAR(255)");
        changes.add(builder.build());
      }
    }

    private void writeAttributes(Variable variable) {
      for(Attribute attribute : variable.getAttributes()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
        builder.tableName(ATTRIBUTE_METADATA_TABLE).withColumn("value_table", valueTable.getSqlName()).withColumn("variable_name", NameConverter.toSqlName(variable.getName())).withColumn("attribute_name", attribute.getName()).withColumn("attribute_value", attribute.getValue().toString());
        changes.add(builder.build());
      }
    }

    private void writeCategories(Variable variable) {
      for(Category category : variable.getCategories()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
        builder.tableName(CATEGORY_METADATA_TABLE).withColumn("value_table", valueTable.getSqlName()).withColumn("variable_name", NameConverter.toSqlName(variable.getName())).withColumn("name", category.getName()).withColumn("code", category.getCode());
        changes.add(builder.build());
      }
    }
  }

  private class JdbcValueSetWriter implements ValueSetWriter {

    InsertDataChangeBuilder insertDataChangeBuilder;

    public JdbcValueSetWriter(VariableEntity entity) {
      insertDataChangeBuilder = new InsertDataChangeBuilder();
      insertDataChangeBuilder.tableName(valueTable.getSqlName()).withColumn(ENTITY_ID_COLUMN, entity.getIdentifier());
    }

    @Override
    public void writeValue(Variable variable, Value value) {
      insertDataChangeBuilder.withColumn(NameConverter.toSqlName(variable.getName()), value.toString());
    }

    @Override
    public void close() throws IOException {
      try {
        InsertDataChange insertDataChange = insertDataChangeBuilder.build();
        for(SqlStatement ss : insertDataChange.generateStatements(valueTable.getDatasource().getDatabase())) {
          System.out.println(ss.getSqlStatement(valueTable.getDatasource().getDatabase()));
        }
        insertDataChange.executeStatements(valueTable.getDatasource().getDatabase(), Collections.EMPTY_LIST);
      } catch(JDBCException e) {
        throw new MagmaRuntimeException(e);
      } catch(UnsupportedChangeException e) {
        throw new MagmaRuntimeException(e);
      }
    }

  }
}