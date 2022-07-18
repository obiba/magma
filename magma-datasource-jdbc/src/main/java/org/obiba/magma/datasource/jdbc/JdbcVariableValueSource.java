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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import liquibase.structure.core.Column;
import org.obiba.magma.*;

import javax.validation.constraints.NotNull;
import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class JdbcVariableValueSource extends AbstractVariableValueSource implements VariableValueSource, VectorSource {
  //
  // Instance Variables
  //

  private final JdbcValueTable valueTable;

  private final Variable variable;

  private final String columnName;

  //
  // Constructors
  //

  JdbcVariableValueSource(JdbcValueTable valueTable, Column column, int idx) {
    this.valueTable = valueTable;
    columnName = column.getName();
    variable = Variable.Builder
        .newVariable(valueTable.getVariableName(columnName), SqlTypes.valueTypeFor(column.getType().getDataTypeId()),
            valueTable.getEntityType()) //
        .index(idx) //
        .repeatable(isMultilines()) //
        .occurrenceGroup(isMultilines() ? valueTable.getSqlName() : null).build();
  }

  JdbcVariableValueSource(JdbcValueTable valueTable, Variable variable) {
    this.valueTable = valueTable;
    this.variable = variable;
    columnName = valueTable.getVariableSqlName(variable.getName());
  }

  //
  // VariableValueSource Methods
  //

  @NotNull
  @Override
  public Variable getVariable() {
    return variable;
  }

  @NotNull
  @Override
  public Value getValue(ValueSet valueSet) {
    JdbcValueSet jdbcValueSet = (JdbcValueSet) valueSet;
    return jdbcValueSet.getValue(variable);
  }

  @NotNull
  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  public boolean supportVectorSource() {
    return true;
  }

  @NotNull
  @Override
  public VectorSource asVectorSource() {
    return this;
  }

  @Override
  public Iterable<Value> getValues(final Iterable<VariableEntity> entities) {
    return () -> {
      try {
        return new ValueIterator(entities);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private boolean isMultilines() {
    return valueTable.isMultilines();
  }

  private class ValueIterator implements Iterator<Value> {

    private final String query;

    private Connection connection;

    private PreparedStatement statement;

    private ResultSet cursor;

    private final Iterator<VariableEntity> entities;

    private final List<List<String>> identifiersPartitions;

    private int partitionIndex = 0;

    private boolean hasNextResults;

    private final Map<String, Value> valueMap = Maps.newHashMap();

    private ValueIterator(Iterable<VariableEntity> entities) throws SQLException {
      JdbcDatasource datasource = valueTable.getDatasource();
      String escapedIdentifierColumn = valueTable.getEntityIdentifierColumnSql();

      this.query = String.format("SELECT %s, %s FROM %s %s ORDER BY %s", escapedIdentifierColumn,
          datasource.escapeColumnName(columnName), datasource.escapeTableName(valueTable.getSqlName()),
          getWhereClause(), escapedIdentifierColumn);
      this.entities = entities.iterator();
      this.identifiersPartitions = Lists.partition(StreamSupport.stream(entities.spliterator(), false)
              .map(VariableEntity::getIdentifier).collect(Collectors.toList()),
          valueTable.getVariableEntityBatchSize());
    }

    @Override
    public boolean hasNext() {
      return entities.hasNext();
    }

    @Override
    public Value next() {
      VariableEntity entity = entities.next();
      String nextId = entity.getIdentifier();

      if (valueMap.containsKey(nextId)) return getValueFromMap(entity);


      try {
        if (cursor == null) {
          cursor = newCursor();
        }

        if (cursor != null) {
          // Scroll until we find the required entity or reach the end of the results
          boolean found = false;
          while (hasNextResults && !found) {
            String id = valueTable.extractEntityIdentifier(cursor);
            valueMap.put(id, getValueFromResult());
            hasNextResults = cursor.next();
            found = nextId.equals(id);
          }
          closeCursorIfNecessary();
        }

        if (valueMap.containsKey(nextId)) return getValueFromMap(entity);
        return getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
      } catch (SQLException e) {
        closeQuietly();
        throw new RuntimeException(e);
      }
    }

    private String getWhereClause() {
      String whereIds = String.format("%s IN (':ids')", valueTable.getEntityIdentifierColumnSql());
      if (!valueTable.getSettings().hasEntityIdentifiersWhere()) return "WHERE " + whereIds;
      else return String.format("WHERE %s AND %s", valueTable.getSettings().getEntityIdentifiersWhere(), whereIds);
    }

    private ResultSet newCursor() throws SQLException {
      if (partitionIndex < identifiersPartitions.size()) {
        List<String> identifiers = identifiersPartitions.get(partitionIndex);
        partitionIndex++;
        String q = query.replace(":ids", Joiner.on("','").join(identifiers));
        connection = valueTable.getDatasource().getJdbcTemplate().getDataSource().getConnection();
        statement = connection.prepareStatement(q);
        cursor = statement.executeQuery();
        hasNextResults = cursor.next();
        return cursor;
      }
      return null;
    }

    @Override
    public void remove() {
      closeQuietly();
      throw new UnsupportedOperationException();
    }

    private Value getValueFromResult() throws SQLException {
      Object resObj = cursor.getObject(columnName);
      if (resObj == null) {
        return variable.isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
      }
      if (variable.isRepeatable()) {
        return getValueType().sequenceOf(resObj.toString());
      }
      return getValueType().valueOf(resObj);
    }

    private void closeCursorIfNecessary() {
      // Close the cursor if we don't have any more results or no more entities to return
      if (!hasNextResults || !hasNext()) {
        closeQuietly();
      }
    }

    /**
     * No duplicate of entities, so remove value from map once get.
     *
     * @param entity
     * @return
     */
    private Value getValueFromMap(VariableEntity entity) {
      Value value = valueMap.get(entity.getIdentifier());
      valueMap.remove(entity.getIdentifier());
      return value;
    }

    private void closeQuietly() {
      closeQuietly(cursor, statement, connection);
      cursor = null;
      connection = null;
      statement = null;
    }

    @SuppressWarnings({"OverlyStrongTypeCast", "ChainOfInstanceofChecks"})
    private void closeQuietly(Object... objs) {
      if (objs != null) {
        for (Object o : objs) {
          try {
            if (o instanceof ResultSet) {
              ((ResultSet) o).close();
            }
            if (o instanceof Statement) {
              ((Statement) o).close();
            }
            if (o instanceof Connection) {
              ((Connection) o).close();
            }
          } catch (SQLException e) {
            // ignored
          }
        }
      }
    }
  }
}
