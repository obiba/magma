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

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.type.LocaleType;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A "line" can be transformed in one or multiple SQL lines.
 */
public class JdbcLine {

  private final boolean multilines;

  @NotNull
  private final VariableEntity entity;

  private final Map<String, Object> columnValueMap;

  private final JdbcValueTable valueTable;

  JdbcLine(@NotNull VariableEntity entity, JdbcValueTable valueTable) {
    this.entity = entity;
    this.valueTable = valueTable;
    this.multilines = valueTable.isMultilines();
    columnValueMap = new LinkedHashMap<>();
    initialize();
  }

  void setValue(Variable variable, Value value) {
    setSingleValue(getVariableSqlName(variable), value);
  }

  Set<String> getColumnNames() {
    return columnValueMap.keySet();
  }

  Collection<Object> getValues() {
    return columnValueMap.values();
  }

  int size() {
    return columnValueMap.size();
  }

  //
  // Private methods
  //

  private void initialize() {
    java.util.Date timestamp = new java.util.Date();
    if (valueTable.hasValueSet(entity)) {
      if (valueTable.hasUpdatedTimestampColumn()) {
        setValue(valueTable.getUpdatedTimestampColumnName(), timestamp);
      }
    } else {
      if (valueTable.hasCreatedTimestampColumn()) {
        setValue(valueTable.getCreatedTimestampColumnName(), timestamp);
      }
      if (valueTable.hasUpdatedTimestampColumn()) {
        setValue(valueTable.getUpdatedTimestampColumnName(), timestamp);
      }
    }
  }

  private void setValue(String columnName, Object columnValue) {
    columnValueMap.put(columnName, columnValue);
  }

  private void setSingleValue(String columnName, Value value) {
    columnValueMap.put(columnName, toColumnValue(value));
  }

  private String getVariableSqlName(Variable variable) {
    return valueTable.getVariableSqlName(variable.getName());
  }

  private Object toColumnValue(Value value) {
    Object columnValue = null;
    if(!value.isNull()) {
      if(value.isSequence()) {
        columnValue = value.toString();
      } else {
        columnValue = value.getValue();

        // Persist some objects as strings.
        if(value.getValueType() == LocaleType.get() || value.getValueType().isGeo()) {
          columnValue = value.toString();
        }
      }
    }
    return columnValue;
  }
}
