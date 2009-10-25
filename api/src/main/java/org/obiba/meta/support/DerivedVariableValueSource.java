package org.obiba.meta.support;

import org.obiba.meta.IValueSetReference;
import org.obiba.meta.IValueSource;
import org.obiba.meta.IVariable;
import org.obiba.meta.IVariableValueSource;
import org.obiba.meta.Value;
import org.obiba.meta.ValueType;

public class DerivedVariableValueSource implements IVariableValueSource {

  private IVariable variable;

  private IValueSource source;

  @Override
  public IVariable getVariable() {
    return variable;
  }

  @Override
  public Value getValue(IValueSetReference valueSetReference) {
    return source.getValue(valueSetReference);
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

}
