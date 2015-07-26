package org.obiba.magma.datasource.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JdbcValueSet extends ValueSetBean {

  private final Map<String, Value> resultSetCache;

  private final String sqlTableName;

  private RowMapper<Map<String, Value>> mapper;

  public JdbcValueSet(final JdbcValueTable valueTable, VariableEntity variableEntity) {
    super(valueTable, variableEntity);
    resultSetCache = new HashMap<>();
    sqlTableName = valueTable.getSettings().getSqlTableName();

    mapper = new RowMapper<Map<String, Value>>() {
      @Override
      public Map<String, Value> mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Value> res = Maps.newHashMap();

        for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
          if(!getValueTable().getSettings().getEntityIdentifierColumns().contains(rs.getMetaData().getColumnName(i))) {
            String variableName = valueTable.getVariableName(rs.getMetaData().getColumnName(i));
            Value variableValue = SqlTypes.valueTypeFor(rs.getMetaData().getColumnType(i)).valueOf(rs.getObject(i));
            res.put(variableName, variableValue);
          }
        }

        return res;
      }
    };
  }

  @NotNull
  @Override
  public JdbcValueTable getValueTable() {
    return (JdbcValueTable) super.getValueTable();
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new JdbcTimestamps(this);
  }

  public Value getValue(Variable variable) {
    if(variable.getValueType().isBinary()) {
      List<Map<String, Value>> res = loadValues(
          Lists.newArrayList(getValueTable().getVariableSqlName(variable.getName())), mapper);

      if(!res.isEmpty()) return res.get(0).get(variable.getName());
    }

    loadResultSetCache();

    return resultSetCache.get(variable.getName());
  }

  private synchronized void loadResultSetCache() {
    if(resultSetCache.isEmpty()) {
      final List<Map<String, Value>> rows = loadValues(getNonBinaryColumns(), mapper);

      for(Map<String, Value> row : rows) {
        resultSetCache.putAll(row);
      }
    }
  }

  private List<String> getNonBinaryColumns() {
    return ImmutableList
        .copyOf(Iterables.filter(Iterables.transform(getValueTable().getVariables(), new Function<Variable, String>() {
          @Nullable
          @Override
          public String apply(@Nullable Variable input) {
            if(input.getValueType().isBinary()) return null;

            return getValueTable().getVariableSqlName(input.getName());
          }
        }), Predicates.notNull()));
  }

  private <T> List<T> loadValues(List<String> columnNames, RowMapper<T> rowMapper) {
    List<String> entityIdentifierColumns = getValueTable().getSettings().getEntityIdentifierColumns();
    String whereClause = Joiner.on(" AND ")
        .join(Iterables.transform(entityIdentifierColumns, new Function<String, String>() {
          @Nullable
          @Override
          public String apply(String idColName) {
            return String.format("%s = ?", idColName);
          }
        }));

    StringBuilder sql = new StringBuilder();
    sql.append(String
        .format("SELECT %s FROM %s WHERE %s", Joiner.on(", ").join(columnNames), sqlTableName, whereClause));
    String[] entityIdentifierColumnValues = getVariableEntity().getIdentifier().split("-");

    return getValueTable().getDatasource().getJdbcTemplate()
        .query(sql.toString(), entityIdentifierColumnValues, rowMapper);
  }

  public Value getCreated() {
    loadResultSetCache();
    return resultSetCache.get(getValueTable().getCreatedTimestampColumnName());
  }

  public Value getUpdated() {
    loadResultSetCache();
    return resultSetCache.get(getValueTable().getUpdatedTimestampColumnName());
  }
}
