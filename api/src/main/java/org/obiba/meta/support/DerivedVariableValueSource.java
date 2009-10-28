package org.obiba.meta.support;

import org.obiba.meta.ValueSetReference;
import org.obiba.meta.ValueSource;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.Value;
import org.obiba.meta.ValueType;

public class DerivedVariableValueSource implements VariableValueSource {

  private Variable variable;

  private ValueSource source;

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Override
  public Value getValue(ValueSetReference valueSetReference) {
    return source.getValue(valueSetReference);
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

}
