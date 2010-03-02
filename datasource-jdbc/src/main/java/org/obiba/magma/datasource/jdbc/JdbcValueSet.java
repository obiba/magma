package org.obiba.magma.datasource.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.jdbc.support.NameConverter;
import org.obiba.magma.support.ValueSetBean;
import org.springframework.jdbc.core.ResultSetExtractor;

public class JdbcValueSet extends ValueSetBean {
  //
  // Instance Variables
  //

  private Map<String, Value> resultSetCache;

  private String escapedSqlTableName;

  //
  // Constructors
  //

  public JdbcValueSet(JdbcValueTable valueTable, VariableEntity variableEntity) {
    super(valueTable, variableEntity);
    resultSetCache = new HashMap<String, Value>();
  }

  //
  // ValueSetBean Methods
  //

  @Override
  public JdbcValueTable getValueTable() {
    return (JdbcValueTable) super.getValueTable();
  }

  //
  // Methods
  //

  public Value getValue(Variable variable) {
    if(resultSetCache.isEmpty()) {
      loadValues();
    }
    return resultSetCache.get(variable.getName());
  }

  private void loadValues() {
    // MAGMA-100
    if(escapedSqlTableName == null) {
      String sqlTableName = ((JdbcValueTable) getValueTable()).getSettings().getSqlTableName();
      escapedSqlTableName = ((JdbcValueTable) getValueTable()).getDatasource().escapeSqlTableName(sqlTableName);
    }

    // Build the SQL query.
    StringBuilder sql = new StringBuilder();

    // ...select all columns
    sql.append("SELECT * ");

    // ...from the mapped table
    sql.append("FROM ");
    sql.append(escapedSqlTableName);

    // ...for the specified entity
    sql.append(" WHERE ");
    List<String> entityIdentifierColumns = ((JdbcValueTable) getValueTable()).getSettings().getEntityIdentifierColumns();
    for(int i = 0; i < entityIdentifierColumns.size(); i++) {
      sql.append(entityIdentifierColumns.get(i));
      sql.append(" = ?");

      if(i < entityIdentifierColumns.size() - 1) {
        sql.append(" AND ");
      }
    }

    // Execute the query.
    String[] entityIdentifierColumnValues = getVariableEntity().getIdentifier().split("-");
    getValueTable().getDatasource().getJdbcTemplate().query(sql.toString(), entityIdentifierColumnValues, new ResultSetExtractor() {
      public Object extractData(ResultSet rs) throws SQLException {
        // Cache the data.
        rs.next();
        for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
          if(!getValueTable().getSettings().getEntityIdentifierColumns().contains(rs.getMetaData().getColumnName(i))) {
            String variableName = NameConverter.toMagmaVariableName(rs.getMetaData().getColumnName(i));
            Value variableValue = SqlTypes.valueTypeFor(rs.getMetaData().getColumnType(i)).valueOf(rs.getObject(i));
            resultSetCache.put(variableName, variableValue);
          }
        }

        // Just return null. We have everything we need in the cache.
        return null;
      }
    });

  }
}
