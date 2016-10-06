/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.support.ValueSetBean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs the SQL query to get one or more {@link org.obiba.magma.ValueSet}s.
 */
public class JdbcValueSetFetcher {

  private final JdbcValueTable valueTable;

  private final String sqlTableName;

  private RowMapper<Map<String, Value>> mapper;

  JdbcValueSetFetcher(final JdbcValueTable valueTable) {
    this.valueTable = valueTable;
    sqlTableName = valueTable.getSettings().getSqlTableName();
    this.mapper = new JdbcRowMapper(valueTable);
  }

  public List<Map<String, Value>> loadNonBinaryVariableValues(List<VariableEntity> entities) {
    return loadValues(getNonBinaryColumns(), entities);
  }

  public List<Map<String, Value>> loadNonBinaryVariableValues(VariableEntity entity) {
    return loadValues(getNonBinaryColumns(), entity);
  }

  public List<Map<String, Value>> loadVariableValues(Variable variable, VariableEntity entity) {
    return loadValues(
        Lists.newArrayList(valueTable.getVariableSqlName(variable.getName())), entity);
  }

  //
  // Private methods
  //

  private List<String> getNonBinaryColumns() {
    List<String> columns = Lists.newArrayList(
        Iterables.filter(Iterables.transform(valueTable.getVariables(), new Function<Variable, String>() {
          @Nullable
          @Override
          public String apply(@Nullable Variable input) {
            if (input.getValueType().isBinary()) return null;

            return valueTable.getVariableSqlName(input.getName());
          }
        }), Predicates.notNull()));

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
    String entityIdentifierColumn = valueTable.getSettings().getEntityIdentifierColumn();
    String whereClause = String.format("%s = ?", datasource.escapeColumnName(entityIdentifierColumn));

    Iterable<String> escapedColumnNames = Iterables.transform(columnNames, new Function<String, String>() {
      @Nullable
      @Override
      public String apply(@Nullable String input) {
        return datasource.escapeColumnName(input);
      }
    });

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
    StringBuilder sql = new StringBuilder();
    sql.append(
        String.format("SELECT %s FROM %s WHERE %s", selectClause, fromClause, whereClause));
    String[] entityIdentifierColumnValues = entity.getIdentifier().split("-");

    return valueTable.getDatasource().getJdbcTemplate()
        .query(sql.toString(), entityIdentifierColumnValues, mapper);
  }

  private List<Map<String, Value>> loadValues(List<String> columnNames, List<VariableEntity> entities) {
    final JdbcDatasource datasource = valueTable.getDatasource();
    String entityIdentifierColumn = valueTable.getSettings().getEntityIdentifierColumn();

    String whereClause = String.format("%s IN (:ids)", datasource.escapeColumnName(entityIdentifierColumn));

    ImmutableList.Builder<String> escapedColumnNames = ImmutableList.builder();
    for (String columnName : columnNames) {
      escapedColumnNames.add(datasource.escapeColumnName(columnName));
    }
    escapedColumnNames.add(datasource.escapeColumnName(entityIdentifierColumn));

    return queryValues(Joiner.on(", ").join(escapedColumnNames.build()), datasource.escapeTableName(sqlTableName), whereClause, entities);
  }

  private List<Map<String, Value>> queryValues(String selectClause, String fromClause, String whereClause, List<VariableEntity> entities) {
    StringBuilder sql = new StringBuilder();
    sql.append(
        String.format("SELECT %s FROM %s WHERE %s", selectClause, fromClause, whereClause));

    MapSqlParameterSource parameters = new MapSqlParameterSource();
    ImmutableList.Builder<String> ids = ImmutableList.builder();
    for (VariableEntity entity : entities) {
      ids.add(entity.getIdentifier());
    }
    parameters.addValue("ids", ids.build());

    return valueTable.getDatasource().getNamedParameterJdbcTemplate().query(sql.toString(), parameters, mapper);
  }
}
