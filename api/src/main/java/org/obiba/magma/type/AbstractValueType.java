package org.obiba.magma.type;

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

public abstract class AbstractValueType implements ValueType {

  private static final long serialVersionUID = -2655334789781837332L;

  @Override
  public Value nullValue() {
    return Factory.newValue(this, null);
  }
}
