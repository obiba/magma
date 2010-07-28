package org.obiba.magma.datasource.csv;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;

/**
 * Used in two cases: 1. When no variables.csv is provided and the variables are inferred from the header of the
 * data.csv file. 2. When the variables provided are a reference from another table.
 * 
 */
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
