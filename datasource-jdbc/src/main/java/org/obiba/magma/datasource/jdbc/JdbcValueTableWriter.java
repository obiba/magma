/**
 * 
 */
package org.obiba.magma.datasource.jdbc;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import liquibase.change.AddColumnChange;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.InsertDataChange;
import liquibase.change.ModifyColumnChange;
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
import org.obiba.magma.type.TextType;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcValueTableWriter implements ValueTableWriter {
  //
  // Constants
  //

  static final String VARIABLE_METADATA_TABLE = "variables";

  static final String ATTRIBUTE_METADATA_TABLE = "variable_attributes";

  static final String CATEGORY_METADATA_TABLE = "categories";

  static final String VALUE_TABLE_COLUMN = "value_table";

  static final String VARIABLE_NAME_COLUMN = "variable_name";

  static final String VALUE_TYPE_COLUMN = "value_type";

  static final String ATTRIBUTE_NAME_COLUMN = "attribute_name";

  static final String ATTRIBUTE_VALUE_COLUMN = "attribute_value";

  static final String CATEGORY_NAME_COLUMN = "name";

  static final String CATEGORY_CODE_COLUMN = "code";

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
      valueTable.getDatasource().databaseChanged();
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
      column.setType(SqlTypes.sqlTypeFor(variable.getValueType(), variable.getValueType().equals(TextType.get()) ? SqlTypes.TEXT_TYPE_HINT_MEDIUM : null));

      if(variableExists(variable)) {
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

    protected boolean variableExists(Variable variable) {
      String columnName = NameConverter.toSqlName(variable.getName());
      return valueTable.getDatasource().getDatabaseSnapshot().getColumn(valueTable.getSqlName(), columnName) != null;
    }
  }

  private class JdbcMetadataVariableWriter extends JdbcVariableWriter {
    //
    // Constants
    //

    private static final String DELETE_VARIABLE_SQL = //
    "DELETE FROM " + VARIABLE_METADATA_TABLE + " WHERE value_table = ? AND name = ?";

    private static final String DELETE_VARIABLE_ATTRIBUTES_SQL = //
    "DELETE FROM " + ATTRIBUTE_METADATA_TABLE + " WHERE value_table = ? AND variable_name = ?";

    private static final String DELETE_VARIABLE_CATEGORIES_SQL = //
    "DELETE FROM " + CATEGORY_METADATA_TABLE + " WHERE value_table = ? AND variable_name = ?";

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
      boolean variableExists = variableExists(variable);

      // For an EXISTING variable, delete the existing metadata.
      if(variableExists) {
        JdbcTemplate jdbcTemplate = valueTable.getDatasource().getJdbcTemplate();
        jdbcTemplate.update(DELETE_VARIABLE_SQL, new Object[] { valueTable.getSqlName(), variable.getName() });
        jdbcTemplate.update(DELETE_VARIABLE_ATTRIBUTES_SQL, new Object[] { valueTable.getSqlName(), variable.getName() });
        jdbcTemplate.update(DELETE_VARIABLE_CATEGORIES_SQL, new Object[] { valueTable.getSqlName(), variable.getName() });
      }

      // For ALL variables (existing and new), insert the new metadata.
      InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
      builder.tableName(VARIABLE_METADATA_TABLE).withColumn(VALUE_TABLE_COLUMN, valueTable.getName()).withColumn("name", variable.getName()).withColumn(VALUE_TYPE_COLUMN, variable.getValueType().getName()).withColumn("mime_type", variable.getMimeType()).withColumn("units", variable.getUnit()).withColumn("is_repeatable", variable.isRepeatable()).withColumn("occurrence_group", variable.getOccurrenceGroup(), true);
      changes.add(builder.build());

      if(variable.hasAttributes()) {
        writeAttributes(variable);
      }
      if(variable.hasCategories()) {
        writeCategories(variable);
      }

      // For a NEW variable, call the superclass's method to add the necessary column the value table.
      if(!variableExists) {
        super.doWriteVariable(variable);
      }
    }

    //
    // Methods
    //

    private void createMetadataTablesIfNotPresent() {
      if(valueTable.getDatasource().getDatabaseSnapshot().getTable(VARIABLE_METADATA_TABLE) == null) {
        CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
        builder.tableName(VARIABLE_METADATA_TABLE).withPrimaryKeyColumn(VALUE_TABLE_COLUMN, "VARCHAR(255)").withPrimaryKeyColumn("name", "VARCHAR(255)").withColumn(VALUE_TYPE_COLUMN, "VARCHAR(255)").withNullableColumn("mime_type", "VARCHAR(255)").withNullableColumn("units", "VARCHAR(255)").withColumn("is_repeatable", "BOOLEAN").withNullableColumn("occurrence_group", "VARCHAR(255)");
        changes.add(builder.build());
      }
      if(valueTable.getDatasource().getDatabaseSnapshot().getTable(ATTRIBUTE_METADATA_TABLE) == null) {
        CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
        builder.tableName(ATTRIBUTE_METADATA_TABLE).withPrimaryKeyColumn(VALUE_TABLE_COLUMN, "VARCHAR(255)").withPrimaryKeyColumn(VARIABLE_NAME_COLUMN, "VARCHAR(255)").withPrimaryKeyColumn(ATTRIBUTE_NAME_COLUMN, "VARCHAR(255)").withColumn(ATTRIBUTE_VALUE_COLUMN, "VARCHAR(255)");
        changes.add(builder.build());
      }
      if(valueTable.getDatasource().getDatabaseSnapshot().getTable(CATEGORY_METADATA_TABLE) == null) {
        CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
        builder.tableName(CATEGORY_METADATA_TABLE).withPrimaryKeyColumn(VALUE_TABLE_COLUMN, "VARCHAR(255)").withPrimaryKeyColumn(VARIABLE_NAME_COLUMN, "VARCHAR(255)").withPrimaryKeyColumn(CATEGORY_NAME_COLUMN, "VARCHAR(255)").withNullableColumn(CATEGORY_CODE_COLUMN, "VARCHAR(255)");
        changes.add(builder.build());
      }
    }

    private void writeAttributes(Variable variable) {
      for(Attribute attribute : variable.getAttributes()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
        builder.tableName(ATTRIBUTE_METADATA_TABLE).withColumn(VALUE_TABLE_COLUMN, valueTable.getSqlName()).withColumn(VARIABLE_NAME_COLUMN, NameConverter.toSqlName(variable.getName())).withColumn(ATTRIBUTE_NAME_COLUMN, attribute.getName()).withColumn(ATTRIBUTE_VALUE_COLUMN, attribute.getValue().toString());
        changes.add(builder.build());
      }
    }

    private void writeCategories(Variable variable) {
      for(Category category : variable.getCategories()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
        builder.tableName(CATEGORY_METADATA_TABLE).withColumn(VALUE_TABLE_COLUMN, valueTable.getSqlName()).withColumn(VARIABLE_NAME_COLUMN, NameConverter.toSqlName(variable.getName())).withColumn(CATEGORY_NAME_COLUMN, category.getName()).withColumn(CATEGORY_CODE_COLUMN, category.getCode());
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
      if(isBooleanValue(value)) {
        Boolean booleanValue = (Boolean) value.getValue();
        insertDataChangeBuilder.withColumn(NameConverter.toSqlName(variable.getName()), booleanValue);
      } else if(isDateValue(value)) {
        Date dateValue = (Date) value.getValue();
        insertDataChangeBuilder.withColumn(NameConverter.toSqlName(variable.getName()), dateValue);
      } else {
        insertDataChangeBuilder.withColumn(NameConverter.toSqlName(variable.getName()), value.toString());
      }
    }

    private boolean isBooleanValue(Value value) {
      return value.getValueType().getJavaClass().equals(Boolean.class) && !value.isSequence();
    }

    private boolean isDateValue(Value value) {
      return value.getValueType().getJavaClass().equals(Date.class) && !value.isSequence();
    }

    @Override
    public void close() throws IOException {
      try {
        InsertDataChange insertDataChange = insertDataChangeBuilder.build();
        List<SqlVisitor> sqlVisitors = Collections.emptyList();
        insertDataChange.executeStatements(valueTable.getDatasource().getDatabase(), sqlVisitors);
      } catch(JDBCException e) {
        throw new MagmaRuntimeException(e);
      } catch(UnsupportedChangeException e) {
        throw new MagmaRuntimeException(e);
      }
    }

  }
}