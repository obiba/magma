package org.obiba.meta.xstream.converter;

import org.obiba.meta.MetaEngine;
import org.obiba.meta.ValueType;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

public class ValueTypeConverter extends AbstractSingleValueConverter {

  public ValueTypeConverter() {
  }

  @Override
  public boolean canConvert(Class type) {
    return ValueType.class.isAssignableFrom(type);
  }

  @Override
  public String toString(Object obj) {
    return ((ValueType) obj).getName();
  }

  @Override
  public Object fromString(String str) {
    return MetaEngine.get().getValueTypeFactory().forName(str);
  }

}
