package org.obiba.magma.type;

import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueType;

/**
 * Converts {@code integer} to {@code decimal} and vice-versa
 */
public class NumericValueConverter implements ValueConverter {

  @Override
  public boolean converts(ValueType from, ValueType to) {
    return from.isNumeric() && to.isNumeric();
  }

  @Override
  public Value convert(Value value, ValueType to) {
    // When converting decimal to integer, this will truncate the decimal places: 0.9 -> 0
    return to.valueOf((Number) value.getValue());
  }

}
