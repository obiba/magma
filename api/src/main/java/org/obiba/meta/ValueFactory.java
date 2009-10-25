package org.obiba.meta;

import java.io.Serializable;

public class ValueFactory {

  ValueFactory() {

  }

  public Value newValue(ValueType type, Object o) {
    // TODO: Use ValueType to create the proper Value object for the Object's real type.
    return new Value(type, (Serializable) o);
  }
}
