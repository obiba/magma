/**
 * 
 */
package org.obiba.magma.datasource.jdbc;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collections;

import liquibase.change.ColumnConfig;
import liquibase.change.CreateTableChange;
import liquibase.change.InsertDataChange;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;

public class JdbcValueTableWriter implements ValueTableWriter {

  private final JdbcValueTable valueTable;

  JdbcValueTableWriter(JdbcValueTable valueTable) {
    super();
    this.valueTable = valueTable;
  }

  @Override
  public ValueSetWriter writeValueSet(VariableEntity entity) {
    return new JdbcMartValueSetWriter(entity);
  }

  @Override
  public VariableWriter writeVariables() {
    return new JdbcMartVariableWriter();
  }

  @Override
  public void close() throws IOException {
    try {
      valueTable.getDatasource().getDatabase().commit();
    } catch(JDBCException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  private class JdbcMartVariableWriter implements VariableWriter {

    private CreateTableChange ctc = new CreateTableChange();

    JdbcMartVariableWriter() {
      ctc = new CreateTableChange();
      ctc.setTableName(valueTable.getSqlName());
      ColumnConfig column = new ColumnConfig();
      column.setName("entity_id");
      column.setType("VARCHAR");
      ctc.addColumn(column);
    }

    @Override
    public void writeVariable(Variable variable) {
      if(!valueTable.isForEntityType(variable.getEntityType())) {
        throw new InvalidParameterException("Wrong entity type for variable '" + variable.getName() + "': " + valueTable.getEntityType() + " expected, " + variable.getEntityType() + " received.");
      }

      InsertDataChange idc = new InsertDataChange();
      idc.setTableName("variables");

      ColumnConfig column = new ColumnConfig();
      column.setName("name");
      column.setValue(variable.getName());
      idc.addColumn(column);

      try {
        idc.executeStatements(valueTable.getDatasource().getDatabase(), Collections.EMPTY_LIST);
      } catch(JDBCException e) {
        throw new MagmaRuntimeException(e);
      } catch(UnsupportedChangeException e) {
        throw new MagmaRuntimeException(e);
      }

      column = new ColumnConfig();
      column.setName(valueTable.getSqlName(variable));
      column.setType("VARCHAR");
      ctc.addColumn(column);
    }

    @Override
    public void close() throws IOException {
      try {
        ctc.executeStatements(valueTable.getDatasource().getDatabase(), Collections.EMPTY_LIST);
      } catch(JDBCException e) {
        throw new MagmaRuntimeException(e);
      } catch(UnsupportedChangeException e) {
        throw new MagmaRuntimeException(e);
      }
    }
  }

  private class JdbcMartValueSetWriter implements ValueSetWriter {

    InsertDataChange change;

    public JdbcMartValueSetWriter(VariableEntity entity) {
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