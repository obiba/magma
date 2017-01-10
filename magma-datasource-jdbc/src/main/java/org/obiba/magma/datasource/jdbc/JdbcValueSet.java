/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provide the {@link Value}s for a {@link VariableEntity}. Non binary data are cached, whereas the binary ones are
 * fetched upon request (more database extractions but less memory usage).
 */
public class JdbcValueSet extends ValueSetBean {

  private final Map<String, Value> resultSetCache = Maps.newHashMap();

  private final JdbcValueSetFetcher fetcher;

  public JdbcValueSet(final JdbcValueTable valueTable, VariableEntity variableEntity) {
    super(valueTable, variableEntity);
    this.fetcher = new JdbcValueSetFetcher(valueTable);
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
    if (variable.getValueType().isBinary()) return getBinaryValue(variable);
    loadResultSetCache();
    Value value = convertValue(variable, resultSetCache.get(variable.getName()));
    resultSetCache.put(variable.getName(), value);
    return value;
  }

  public Value getCreated() {
    loadResultSetCache();
    if (!getValueTable().hasCreatedTimestampColumn()) return null;
    String createdColName = getValueTable().getCreatedTimestampColumnName();
    if (resultSetCache.containsKey(createdColName)) return resultSetCache.get(createdColName);
    return null;
  }

  public Value getUpdated() {
    loadResultSetCache();
    if (!getValueTable().hasUpdatedTimestampColumn()) return null;
    String updatedColName = getValueTable().getUpdatedTimestampColumnName();
    if (resultSetCache.containsKey(updatedColName)) return resultSetCache.get(updatedColName);
    return null;
  }

  //
  // Private methods
  //

  private Value getBinaryValue(Variable variable) {
    List<Map<String, Value>> res = fetcher.loadVariableValues(variable, getVariableEntity());
    if (res.isEmpty())
      return variable.isRepeatable() ? variable.getValueType().nullSequence() : variable.getValueType().nullValue();
    Value value = variable.isRepeatable() && getValueTable().isMultilines() ?
        variable.getValueType().sequenceOf(res.stream().map(m -> m.get(variable.getName())).collect(Collectors.toList()))
        : res.get(0).get(variable.getName());
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
    if (value == null)
      return variable.isRepeatable() ? variable.getValueType().nullSequence() : variable.getValueType().nullValue();
    if (value.getValueType() != variable.getValueType()) {
      return variable.isRepeatable() ? convertToSequence(variable, value) : variable.getValueType().convert(value);
    }
    if (variable.isRepeatable() && !value.isSequence()) {
      return convertToSequence(variable, value);
    }
    if (!variable.isRepeatable() && value.isSequence()) {
      return value.asSequence().getSize() == 0 ? variable.getValueType().nullValue() : value.asSequence().get(0);
    }
    return value;
  }

  private Value convertToSequence(Variable variable, Value value) {
    if (value.isSequence()) return value;
    if (getValueTable().isMultilines()) {
      // the observed value is not a sequence because there was only one line read for the entity
      return variable.getValueType().sequenceOf(Lists.newArrayList(value));
    }
    return value.isNull()
        ? variable.getValueType().nullSequence()
        : variable.getValueType().sequenceOf(value.toString());
  }

  private synchronized void loadResultSetCache() {
    if (resultSetCache.isEmpty()) {
      populateResultSetCache(fetcher.loadNonBinaryVariableValues(getVariableEntity()));
    }
  }

  void populateResultSetCache(List<Map<String, Value>> rows) {
    rows.forEach(row -> {
      row.forEach((key, value) -> {
        if (resultSetCache.containsKey(key)) {
          Value current = resultSetCache.get(key);
          if (current.isSequence()) {
            ((List<Value>)resultSetCache.get(key).asSequence().getValue()).add(value);
          } else {
            resultSetCache.put(key, current.getValueType().sequenceOf(Lists.newArrayList(current, value)));
          }
        } else {
          resultSetCache.put(key, value);
        }
      });
    });
  }
}
