package org.obiba.magma.datasource.csv;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;

public class CsvVariableValueSource implements VariableValueSource {

  private Variable variable;

  public CsvVariableValueSource(Variable variable) {
    this.variable = variable;
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  public Value getValue(ValueSet valueSet) {
    return ((CsvValueSet) valueSet).getValue(variable);
  }

}
