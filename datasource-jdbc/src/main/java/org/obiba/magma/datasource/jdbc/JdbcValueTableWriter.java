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

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.jdbc.support.NameConverter;

public class JdbcValueTableWriter implements ValueTableWriter {
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
    return new JdbcVariableWriter();
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

    private List<Change> changes = new ArrayList<Change>();

    //
    // VariableWriter Methods
    //

    @Override
    public void writeVariable(Variable variable) {
      if(!valueTable.isForEntityType(variable.getEntityType())) {
        throw new InvalidParameterException("Wrong entity type for variable '" + variable.getName() + "': " + valueTable.getEntityType() + " expected, " + variable.getEntityType() + " received.");
      }

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

        valueTable.addVariableValueSource(new JdbcValueTable.JdbcVariableValueSource(valueTable.getEntityType(), column));
      }
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
  }

  private class JdbcValueSetWriter implements ValueSetWriter {

    InsertDataChange change;

    public JdbcValueSetWriter(VariableEntity entity) {
      change = new InsertDataChange();
      change.setTableName(valueTable.getSqlName());

      ColumnConfig column = new ColumnConfig();
      column.setName("entity_id");
      column.setValue(entity.getIdentifier());
      change.addColumn(column);
    }

    @Override
    public void writeValue(Variable variable, Value value) {
      ColumnConfig column = new ColumnConfig();
      column.setName(valueTable.getSqlName(variable));
      column.setValue(value.toString());
      change.addColumn(column);
    }

    @Override
    public void close() throws IOException {
      try {
        for(SqlStatement ss : change.generateStatements(valueTable.getDatasource().getDatabase())) {
          System.out.println(ss.getSqlStatement(valueTable.getDatasource().getDatabase()));
        }
        change.executeStatements(valueTable.getDatasource().getDatabase(), Collections.EMPTY_LIST);
      } catch(JDBCException e) {
        throw new MagmaRuntimeException(e);
      } catch(UnsupportedChangeException e) {
        throw new MagmaRuntimeException(e);
      }
    }

  }

}