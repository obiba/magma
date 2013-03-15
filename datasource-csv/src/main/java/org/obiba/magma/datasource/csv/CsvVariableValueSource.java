package org.obiba.magma.datasource.csv;

import javax.annotation.Nonnull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;

/**
 * Used in two cases:
 * <ol>
 * <li>When no variables.csv is provided and the variables are inferred from the header of the data.csv file</li>
 * <li>When the variables provided are a reference from another table</li>
 * </ol>
 */
public class CsvVariableValueSource implements VariableValueSource {

  private final Variable variable;

  public CsvVariableValueSource(Variable variable) {
    this.variable = variable;
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Nonnull
  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Nonnull
  @Override
  public Value getValue(ValueSet valueSet) {
    return ((CsvValueSet) valueSet).getValue(variable);
  }

  @Override
  public VectorSource asVectorSource() {
    return null;
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
