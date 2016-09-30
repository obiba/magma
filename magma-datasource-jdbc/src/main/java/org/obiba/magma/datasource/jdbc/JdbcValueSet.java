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
import com.google.common.base.Strings;
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
    return new JdbcValueSetTimestamps(this);
  }

  public Value getValue(Variable variable) {
    if(variable.getValueType().isBinary()) return getBinaryValue(variable);

    loadResultSetCache();

    Value value = convertValue(variable, resultSetCache.get(variable.getName()));
    resultSetCache.put(variable.getName(), value);
    return value;
  }

  private Value getBinaryValue(Variable variable) {
    List<Map<String, Value>> res = loadValues(
        Lists.newArrayList(getValueTable().getVariableSqlName(variable.getName())), mapper);

    if(res.isEmpty())
      return variable.isRepeatable() ? variable.getValueType().nullSequence() : variable.getValueType().nullValue();

    Value value = res.get(0).get(variable.getName());
    return convertValue(variable, value);
  }

  /**
   * Convert the value as loaded from the SQL table  into the expected variable value type (column type
   * may not match exactly the variable value type).
   *
   * @param variable
   * @param value
   * @return
   */
  private Value convertValue(Variable variable, Value value) {
    if (value == null) return variable.isRepeatable() ? variable.getValueType().nullSequence() : variable.getValueType().nullValue();
    if(value.getValueType() != variable.getValueType()) {
      return variable.isRepeatable() ? convertToSequence(variable, value) : variable.getValueType().convert(value);
    }
    if(variable.isRepeatable() && !value.isSequence()) {
      return convertToSequence(variable, value);
    }
    return value;
  }

  private Value convertToSequence(Variable variable, Value value) {
    return value.isNull()
        ? variable.getValueType().nullSequence()
        : variable.getValueType().sequenceOf(value.toString());
  }

  private synchronized void loadResultSetCache() {
    if(resultSetCache.isEmpty()) {
      List<Map<String, Value>> rows = loadValues(getNonBinaryColumns(), mapper);

      for(Map<String, Value> row : rows) {
        resultSetCache.putAll(row);
      }
    }
  }

  private List<String> getNonBinaryColumns() {
    List<String> columns = Lists.newArrayList(
        Iterables.filter(Iterables.transform(getValueTable().getVariables(), new Function<Variable, String>() {
          @Nullable
          @Override
          public String apply(@Nullable Variable input) {
            if(input.getValueType().isBinary()) return null;

            return getValueTable().getVariableSqlName(input.getName());
          }
        }), Predicates.notNull()));

    String created = getValueTable().getCreatedTimestampColumnName();

    if(!Strings.isNullOrEmpty(created)) columns.add(created);

    String updated = getValueTable().getUpdatedTimestampColumnName();

    if(!Strings.isNullOrEmpty(updated)) columns.add(updated);

    return columns;
  }

  private <T> List<T> loadValues(List<String> columnNames, RowMapper<T> rowMapper) {
    final JdbcDatasource datasource = getValueTable().getDatasource();
    List<String> entityIdentifierColumns = getValueTable().getSettings().getEntityIdentifierColumns();

    String whereClause = Joiner.on(" AND ")
        .join(Iterables.transform(entityIdentifierColumns, new Function<String, String>() {
          @Nullable
          @Override
          public String apply(String idColName) {
            return String.format("%s = ?", datasource.escapeColumnName(idColName));
          }
        }));

    Iterable<String> escapedColumnNames = Iterables.transform(columnNames, new Function<String, String>() {
      @Nullable
      @Override
      public String apply(@Nullable String input) {
        return datasource.escapeColumnName(input);
      }
    });

    StringBuilder sql = new StringBuilder();
    sql.append(
        String.format("SELECT %s FROM %s WHERE %s", Joiner.on(", ").join(escapedColumnNames), datasource.escapeTableName(sqlTableName), whereClause));
    String[] entityIdentifierColumnValues = getVariableEntity().getIdentifier().split("-");

    return datasource.getJdbcTemplate()
        .query(sql.toString(), entityIdentifierColumnValues, rowMapper);
  }

  public Value getCreated() {
    loadResultSetCache();

    if(!getValueTable().hasCreatedTimestampColumn()) return null;

    String createdColName = getValueTable().getCreatedTimestampColumnName();

    if(resultSetCache.containsKey(createdColName)) return resultSetCache.get(createdColName);

    return null;
  }

  public Value getUpdated() {
    loadResultSetCache();

    if(!getValueTable().hasUpdatedTimestampColumn()) return null;

    String updatedColName = getValueTable().getUpdatedTimestampColumnName();

    if(resultSetCache.containsKey(updatedColName)) return resultSetCache.get(updatedColName);

    return null;
  }
}
