/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.spss.support.SpssVariableTypeMapper;
import org.obiba.magma.datasource.spss.support.SpssVariableValueFactory;
import org.obiba.magma.support.ValueSetBean;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSVariable;

public class SpssValueSet extends ValueSetBean {

  private final Map<String, Value> row = new HashMap<>();

  private final SPSSFile spssFile;

  private final Map<String, Integer> identifierToVariableIndex;

  public SpssValueSet(ValueTable table, VariableEntity entity, SPSSFile spssFile,
      Map<String, Integer> map) {
    super(table, entity);
    this.spssFile = spssFile;
    identifierToVariableIndex = map;
    loadVariables();
  }

  @NotNull
  @Override
  public SpssValueTable getValueTable() {
    return (SpssValueTable) super.getValueTable();
  }

  public Value getValue(Variable variable) {
    return row.get(variable.getName());
  }

  //
  // Private methods
  //

  private void loadVariables() {
    VariableEntity variableEntity = getVariableEntity();
    int variableIndex = identifierToVariableIndex.get(variableEntity.getIdentifier());

    for(int i = 1; i < spssFile.getVariableCount(); i++) {
      SPSSVariable spssVariable = spssFile.getVariable(i);
      row.put(spssVariable.getName(),
          new SpssVariableValueFactory(variableIndex, spssVariable, SpssVariableTypeMapper.map(spssVariable)).create());
    }
  }

}
