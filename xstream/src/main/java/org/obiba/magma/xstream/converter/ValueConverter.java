package org.obiba.magma.xstream.converter;

import org.obiba.magma.MetaEngine;
import org.obiba.magma.Value;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ValueConverter implements Converter {

  public ValueConverter() {
  }

  @Override
  public boolean canConvert(Class type) {
    return Value.class.isAssignableFrom(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Value value = (Value) source;
    writer.addAttribute("valueType", value.getValueType().getName());
    writer.setValue(value.isNull() ? "null" : value.toString());
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String value = reader.getValue();
    return MetaEngine.get().getValueTypeFactory().forName(reader.getAttribute("valueType")).valueOf(value == null ? null : value);
  }

}
