/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.csv;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.BinaryValueFileHelper;
import org.obiba.magma.type.BinaryType;

/**
 * Use this class to build up a line of csv file to be written to a csv file.
 */
public class CsvLine {

  private Map<String, Integer> headerMap;

  private final Map<String, Value> valueMap;

  private int index = 1;

  private final boolean multilines;

  @NotNull
  private final VariableEntity entity;

  @NotNull
  private final File parent;

  CsvLine(@NotNull VariableEntity entity, @NotNull File parent, boolean multilines) {
    this.entity = entity;
    this.parent = parent;
    this.multilines = multilines;
    headerMap = new HashMap<>();
    valueMap = new HashMap<>();
  }

  void setValue(Variable variable, Value value) {
    if(!headerMap.containsKey(variable.getName())) {
      headerMap.put(variable.getName(), index++);
    }

    Value valueToWrite = variable.getValueType().equals(BinaryType.get()) //
        ? BinaryValueFileHelper.writeValue(parent, variable, entity, value) //
        : value;
    valueMap.put(variable.getName(), valueToWrite);
  }

  List<String[]> getLines() {
    if (multilines) {
      return getMultipleLines();
    } else {
      List<String[]> lines = Lists.newArrayList();
      lines.add(getSingleLine());
      return lines;
    }
  }

  Map<String, Integer> getHeaderMap() {
    return headerMap;
  }

  void setHeaderMap(Map<String, Integer> headerMap) {
    this.headerMap = headerMap;
  }

  //
  // Private methods
  //

  private String[] getSingleLine() {
    String[] line = new String[headerMap.size() + 1];
    line[0] = entity.getIdentifier();
    for(Map.Entry<String, Integer> entry : headerMap.entrySet()) {
      String strValue = null;
      String variableName = entry.getKey();
      if(valueMap.containsKey(variableName)) {
        strValue = valueMap.get(variableName).toString();
      }
      line[entry.getValue()] = strValue;
    }
    return line;
  }

  private List<String[]> getMultipleLines() {
    List<String[]> lines = Lists.newArrayList();
    // first detect the longest value sequence
    int length = 1;
    for (Value value : valueMap.values()) {
      if (value.isSequence()) length = Math.max(length, value.asSequence().getSize());
    }
    for (int i=0; i<length ; i++) {
      lines.add(getMultipleLinesAt(i));
    }
    return lines;
  }

  private String[] getMultipleLinesAt(int position) {
    String[] line = new String[headerMap.size() + 1];
    line[0] = entity.getIdentifier();
    for(Map.Entry<String, Integer> entry : headerMap.entrySet()) {
      String strValue = null;
      String variableName = entry.getKey();
      if(valueMap.containsKey(variableName)) {
        Value value = valueMap.get(variableName);
        if (value.isSequence()) {
          if (position < value.asSequence().getSize()) {
            value = value.asSequence().get(position);
          } else {
            value = value.getValueType().nullValue();
          }
        }
        strValue = value.toString();
      }
      line[entry.getValue()] = strValue;
    }
    return line;
  }

}
