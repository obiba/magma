package org.obiba.magma.datasource.excel;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;

public class ExcelVariableValueSource implements VariableValueSource {

  private Variable variable;

  public ExcelVariableValueSource(Variable variable) {
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
    return null;
  }

}
