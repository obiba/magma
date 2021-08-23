/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import liquibase.structure.core.DataType;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Performs the SQL query to get one or more {@link org.obiba.magma.ValueSet}s.
 */
class JdbcValueSetFetcher {

  private final JdbcValueTable valueTable;

  private final String sqlTableName;

  private final boolean numericIdentifiers;

  private RowMapper<Map<String, Value>> mapper;

  JdbcValueSetFetcher(final JdbcValueTable valueTable) {
    this.valueTable = valueTable;
    sqlTableName = valueTable.getSettings().getSqlTableName();
    this.mapper = new JdbcRowMapper(valueTable);
    numericIdentifiers = SqlTypes.valueTypeFor(valueTable.getEntityIdentifierColumnType().getDataTypeId()).isNumeric();
  }

  List<Map<String, Value>> loadNonBinaryVariableValues(List<VariableEntity> entities) {
    return loadValues(getNonBinaryColumns(), entities);
  }

  List<Map<String, Value>> loadNonBinaryVariableValues(VariableEntity entity) {
    return loadValues(getNonBinaryColumns(), entity);
  }

  List<Map<String, Value>> loadVariableValues(Variable variable, VariableEntity entity) {
    return loadValues(
        Lists.newArrayList(valueTable.getVariableSqlName(variable.getName())), entity);
  }

  //
  // Private methods
  //

  private List<String> getNonBinaryColumns() {
    List<String> columns = valueTable.getVariables().stream()
        .filter(variable -> !variable.getValueType().isBinary())
        .map(variable -> valueTable.getVariableSqlName(variable.getName()))
        .collect(Collectors.toList());

    String created = valueTable.getCreatedTimestampColumnName();
    if (!Strings.isNullOrEmpty(created)) columns.add(created);

    String updated = valueTable.getUpdatedTimestampColumnName();
    if (!Strings.isNullOrEmpty(updated)) columns.add(updated);

    return columns;
  }

  /**
   * Get the rows for the given entity.
   *
   * @param columnNames
   * @param entity
   * @return
   */
  private List<Map<String, Value>> loadValues(List<String> columnNames, VariableEntity entity) {
    final JdbcDatasource datasource = valueTable.getDatasource();
    String entityIdentifierColumn = valueTable.getEntityIdentifierColumn();
    String whereClause = String.format("%s = ?", datasource.escapeColumnName(entityIdentifierColumn));
    if (valueTable.getSettings().hasEntityIdentifiersWhere()) {
      whereClause = String.format("%s AND %s", valueTable.getSettings().getEntityIdentifiersWhere(), whereClause);
    }

    Iterable<String> escapedColumnNames = columnNames.stream().map(datasource::escapeColumnName).collect(Collectors.toList());

    return queryValues(Joiner.on(", ").join(escapedColumnNames), datasource.escapeTableName(sqlTableName), whereClause, entity);
  }

  /**
   * Query the rows for the given entity.
   *
   * @param selectClause
   * @param fromClause
   * @param whereClause
   * @param entity
   * @return
   */
  private List<Map<String, Value>> queryValues(String selectClause, String fromClause, String whereClause, VariableEntity entity) {
    String sql = String.format("SELECT %s FROM %s WHERE %s", selectClause, fromClause, whereClause);
    return valueTable.getDatasource().getJdbcTemplate()
        .query(sql, new Object[] { asIdentifier(entity) }, mapper);
  }

  private List<Map<String, Value>> loadValues(List<String> columnNames, List<VariableEntity> entities) {
    final JdbcDatasource datasource = valueTable.getDatasource();

    String whereClause = String.format("%s IN (:ids)", valueTable.getEntityIdentifierColumnSql());
    if (valueTable.getSettings().hasEntityIdentifiersWhere()) {
      whereClause = String.format("%s AND %s", valueTable.getSettings().getEntityIdentifiersWhere(), whereClause);
    }

    ImmutableList.Builder<String> escapedColumnNames = ImmutableList.builder();
    for (String columnName : columnNames) {
      escapedColumnNames.add(datasource.escapeColumnName(columnName));
    }
    escapedColumnNames.add(valueTable.getEntityIdentifierColumnSql());

    return queryValues(Joiner.on(", ").join(escapedColumnNames.build()), datasource.escapeTableName(sqlTableName), whereClause, entities);
  }

  private List<Map<String, Value>> queryValues(String selectClause, String fromClause, String whereClause, List<VariableEntity> entities) {
    String sql = String.format("SELECT %s FROM %s WHERE %s", selectClause, fromClause, whereClause);

    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("ids", entities.stream().map(this::asIdentifier).collect(Collectors.toList()));

    return valueTable.getDatasource().getNamedParameterJdbcTemplate().query(sql, parameters, mapper);
  }

  private Object asIdentifier(VariableEntity entity) {
    if (numericIdentifiers)
      return new Long(entity.getIdentifier());
    return entity.getIdentifier();
  }
}
