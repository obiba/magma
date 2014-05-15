package org.obiba.magma.type;

import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueType;

public class AnyToTextValueConverter implements ValueConverter {

  @Override
  public boolean converts(ValueType from, ValueType to) {
    return to == TextType.get();
  }

  @Override
  public Value convert(Value value, ValueType to) {
    return TextType.get().valueOf(value.toString());
  }
}
