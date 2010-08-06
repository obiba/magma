package org.obiba.magma.type;

import java.math.BigDecimal;

import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueType;

public class TextToIntegerValueConverter implements ValueConverter {

  @Override
  public boolean converts(ValueType from, ValueType to) {
    return from == TextType.get() && (to == IntegerType.get() || to == DecimalType.get());
  }

  @Override
  public Value convert(Value value, ValueType to) {
    return to.valueOf(new BigDecimal(value.toString()));
  }

}
