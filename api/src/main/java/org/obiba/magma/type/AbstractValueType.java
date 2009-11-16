package org.obiba.meta.type;

import org.obiba.meta.Value;
import org.obiba.meta.ValueType;

public abstract class AbstractValueType implements ValueType {

  private static final long serialVersionUID = -2655334789781837332L;

  @Override
  public Value nullValue() {
    return Factory.newValue(this, null);
  }
}
