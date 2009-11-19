package org.obiba.magma.type;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;

public abstract class AbstractValueType implements ValueType {

  private static final long serialVersionUID = -2655334789781837332L;

  @Override
  public Value nullValue() {
    return Factory.newValue(this, null);
  }

  @Override
  public ValueSequence nullSequence() {
    return Factory.newSequence(this, null);

  }

  @Override
  public ValueSequence sequenceOf(Iterable<Value> values) {
    return Factory.newSequence(this, values);
  }
}
