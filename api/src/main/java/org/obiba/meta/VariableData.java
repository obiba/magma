/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.meta;

/**
 *
 */
public class VariableData implements IVariableData {

  private IVariable variable;

  private IValueSetReference reference;

  private Value value;

  public VariableData(IVariable variable, IValueSetReference reference, Value value) {
    this.variable = variable;
    this.reference = reference;
    this.value = value;
  }

  public IVariable getVariable() {
    return variable;
  }

  @Override
  public IValueSetReference getReference() {
    return reference;
  }

  @Override
  public Value getValue() {
    return value;
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

}
