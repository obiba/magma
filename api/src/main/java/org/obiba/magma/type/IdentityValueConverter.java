package org.obiba.magma.type;

import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueType;

/**
 * A {@code ValueConverter} that does no conversion (applicable when both {@code ValueType}s are the same).
 */
public class IdentityValueConverter implements ValueConverter {

  @Override
  public boolean converts(ValueType from, ValueType to) {
    // Converts values when types are the same
    return from == to;
  }

  @Override
  public Value convert(Value value, ValueType to) {
    // identity
    return value;
  }

}
