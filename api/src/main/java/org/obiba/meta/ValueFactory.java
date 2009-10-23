package org.obiba.meta;

import java.io.Serializable;

public class ValueFactory {

  public static final ValueFactory INSTANCE = new ValueFactory();

  private ValueFactory() {

  }

  public Value newValue(ValueType type, Object o) {
    // TODO
    return new Value(type, (Serializable) o);
  }
}
