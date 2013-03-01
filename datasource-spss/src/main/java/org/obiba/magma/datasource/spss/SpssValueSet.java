/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
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

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.spss.support.SpssValueFactory;
import org.obiba.magma.datasource.spss.support.SpssVariableTypeMapper;
import org.obiba.magma.support.ValueSetBean;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSVariable;

public class SpssValueSet extends ValueSetBean {

  private final Map<String, Value> row = new HashMap<String, Value>();

  private final SPSSFile spssFile;

  public SpssValueSet(ValueTable table, VariableEntity entity, SPSSFile spssFile) {
    super(table, entity);
    this.spssFile = spssFile;
    loadVariables();
  }

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
    SpssVariableEntity variableEntity = (SpssVariableEntity) getVariableEntity();
    int variableIndex = variableEntity.getVariableIndex();
    SpssVariableTypeMapper typeMapper = new SpssVariableTypeMapper();

    for(int i = 1; i < spssFile.getVariableCount(); i++) {
      SPSSVariable spssVariable = spssFile.getVariable(i);
      row.put(spssVariable.getName(), new SpssValueFactory(variableIndex, spssVariable, typeMapper.map(spssVariable)).create());
    }
  }

}
