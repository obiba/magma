/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss;

import java.util.HashMap;
import java.util.List;
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

  private final List<Integer> valuesIndex;

  private final boolean multilines;

  private final int idVariableIndex;

  public SpssValueSet(ValueTable table, VariableEntity entity, int idVariableIndex, SPSSFile spssFile, List<Integer> valuesIndex, boolean multilines) {
    super(table, entity);
    this.spssFile = spssFile;
    this.valuesIndex = valuesIndex;
    this.multilines = multilines;
    this.idVariableIndex = idVariableIndex;
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
    for(int i = 0; i < spssFile.getVariableCount(); i++) {
      if (i != idVariableIndex) {
        SPSSVariable spssVariable = spssFile.getVariable(i);
        row.put(spssVariable.getName(),
            new SpssVariableValueFactory(valuesIndex, spssVariable, SpssVariableTypeMapper.map(spssVariable), false, multilines).create());
      }
    }
  }

}
