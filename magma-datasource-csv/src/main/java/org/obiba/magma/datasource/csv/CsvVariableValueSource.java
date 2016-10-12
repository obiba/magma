/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.csv;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSetWrapper;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.VectorSourceNotSupportedException;

/**
 * Used in two cases:
 * <ol>
 * <li>When no variables.csv is provided and the variables are inferred from the header of the data.csv file</li>
 * <li>When the variables provided are a reference from another table</li>
 * </ol>
 */
public class CsvVariableValueSource extends AbstractVariableValueSource implements VariableValueSource {

  private final Variable variable;

  public CsvVariableValueSource(Variable variable) {
    this.variable = variable;
  }

  @NotNull
  @Override
  public Variable getVariable() {
    return variable;
  }

  @NotNull
  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @NotNull
  @Override
  public Value getValue(ValueSet valueSet) {
    if (valueSet instanceof ValueSetWrapper) {
      return ((CsvValueSet) ((ValueSetWrapper) valueSet).getWrapped()).getValue(variable);
    }
    return ((CsvValueSet) valueSet).getValue(variable);
  }

  @Override
  public boolean supportVectorSource() {
    return false;
  }

  @NotNull
  @Override
  public VectorSource asVectorSource() {
    throw new VectorSourceNotSupportedException(getClass());
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj instanceof CsvVariableValueSource) {
      CsvVariableValueSource that = (CsvVariableValueSource) obj;
      return variable.getName().equals(that.variable.getName());
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return variable.getName().hashCode();
  }
}
