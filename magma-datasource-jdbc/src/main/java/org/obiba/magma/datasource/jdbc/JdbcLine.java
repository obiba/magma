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

import com.google.common.collect.Lists;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.LocaleType;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A "line" can be transformed in one or multiple SQL lines.
 */
class JdbcLine {

  private final boolean multilines;

  @NotNull
  private final VariableEntity entity;

  private final List<String> columnNames = Lists.newArrayList();

  private final List<Value> values = Lists.newArrayList();

  private final JdbcValueTable valueTable;

  JdbcLine(@NotNull VariableEntity entity, JdbcValueTable valueTable) {
    this.entity = entity;
    this.valueTable = valueTable;
    this.multilines = valueTable.isMultilines();
    initialize();
  }

  void setValue(Variable variable, Value value) {
    setValue(getVariableSqlName(variable), value);
  }

  List<String> getColumnNames() {
    return columnNames;
  }

  List<List<Value>> getLines() {
    if (multilines) {
      return getMultipleLines();
    } else {
      List<List<Value>> lines = Lists.newArrayList();
      lines.add(getSingleLine());
      return lines;
    }
  }

  int size() {
    return getColumnNames().size();
  }

  //
  // Private methods
  //

  private void initialize() {
    Value timestamp = DateTimeType.get().now();
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

  /**
   * Convert each {@link Value} into a SQL value for a single line (then sequences are turned into CSV strings).
   * @return
   */
  private List<Value> getSingleLine() {
    return values;
  }

  private void setValue(String columnName, Value value) {
    columnNames.add(columnName);
    values.add(value);
  }

  private String getVariableSqlName(Variable variable) {
    return valueTable.getVariableSqlName(variable.getName());
  }

  public List<List<Value>> getMultipleLines() {
    List<List<Value>> lines = Lists.newArrayList();
    // first detect the longest value sequence
    int length = values.stream().mapToInt(v -> v.isSequence() ? v.asSequence().getSize() : 1).max().orElse(1);

    for (int i=0; i<length ; i++) {
      lines.add(getMultipleLinesAt(i));
    }
    return lines;
  }

  private List<Value> getMultipleLinesAt(int position) {
    List<Value> line = Lists.newArrayListWithExpectedSize(columnNames.size());
    for (Value value : values) {
      Value valueAt = value;
      if (value.isSequence()) {
        if (position < value.asSequence().getSize()) {
          valueAt = value.asSequence().get(position);
        } else {
          valueAt = value.getValueType().nullValue();
        }
      }
      line.add(valueAt);
    }
    return line;
  }
}
