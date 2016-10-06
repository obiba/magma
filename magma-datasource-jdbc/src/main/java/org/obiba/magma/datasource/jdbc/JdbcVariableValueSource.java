package org.obiba.magma.datasource.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;

import com.google.common.collect.Maps;

import liquibase.structure.core.Column;

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

  JdbcVariableValueSource(JdbcValueTable valueTable, Column column) {
    this.valueTable = valueTable;
    columnName = column.getName();
    variable = Variable.Builder
        .newVariable(valueTable.getVariableName(columnName), SqlTypes.valueTypeFor(column.getType().getDataTypeId()),
            valueTable.getEntityType()).build();
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
  public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
    return () -> {
      try {
        return new ValueIterator(valueTable.getDatasource().getJdbcTemplate().getDataSource().getConnection(),
            entities);
      } catch(SQLException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private class ValueIterator implements Iterator<Value> {

    private final Connection connection;

    private final PreparedStatement statement;

    private final ResultSet rs;

    private final Iterator<VariableEntity> entities;

    private boolean hasNextResults;

    private boolean closed = false;

    private final Map<String, Value> valueMap = Maps.newHashMap();

    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private ValueIterator(Connection connection, Iterable<VariableEntity> entities) throws SQLException {
      this.connection = connection;
      JdbcDatasource datasource = valueTable.getDatasource();
      String escapedIdentifierColumn = valueTable.getEntityIdentifierColumnSql();

      statement = connection.prepareStatement(
          String.format("SELECT %s, %s FROM %s %s ORDER BY %s", escapedIdentifierColumn,
              datasource.escapeColumnName(columnName), datasource.escapeTableName(valueTable.getSqlName()),
              getWhereClause(), escapedIdentifierColumn));
      rs = statement.executeQuery();
      hasNextResults = rs.next();
      this.entities = entities.iterator();
      closeCursorIfNecessary();
    }

    private String getWhereClause() {
      if (!valueTable.getSettings().hasEntityIdentifiersWhere()) return "";
      else return String.format("WHERE %s", valueTable.getSettings().getEntityIdentifiersWhere());
    }

    @Override
    public boolean hasNext() {
      return entities.hasNext();
    }

    @Override
    public Value next() {
      VariableEntity entity = entities.next();
      String nextId = entity.getIdentifier();

      if(valueMap.containsKey(nextId)) return getValueFromMap(entity);

      try {
        // Scroll until we find the required entity or reach the end of the results
        boolean found = false;
        while(hasNextResults && !found) {
          String id = valueTable.extractEntityIdentifier(rs);
          valueMap.put(id, getValueFromResult());
          hasNextResults = rs.next();
          found = nextId.equals(id);
        }

        closeCursorIfNecessary();

        if(valueMap.containsKey(nextId)) return getValueFromMap(entity);
        return getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
      } catch(SQLException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    private Value getValueFromResult() throws SQLException {
      Object resObj = rs.getObject(columnName);
      if(resObj == null) {
        return variable.isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
      }
      if(variable.isRepeatable()) {
        return getValueType().sequenceOf(resObj.toString());
      }
      return getValueType().valueOf(resObj);

    }

    private void closeCursorIfNecessary() {
      if(!closed) {
        // Close the cursor if we don't have any more results or no more entities to return
        if(!hasNextResults || !hasNext()) {
          closed = true;
          closeQuietly(rs, statement, connection);
        }
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

    @SuppressWarnings({ "OverlyStrongTypeCast", "ChainOfInstanceofChecks" })
    private void closeQuietly(Object... objs) {
      if(objs != null) {
        for(Object o : objs) {
          try {
            if(o instanceof ResultSet) {
              ((ResultSet) o).close();
            }
            if(o instanceof Statement) {
              ((Statement) o).close();
            }
            if(o instanceof Connection) {
              ((Connection) o).close();
            }
          } catch(SQLException e) {
            // ignored
          }
        }
      }
    }
  }
}
