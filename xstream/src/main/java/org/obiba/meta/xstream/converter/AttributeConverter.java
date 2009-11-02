package org.obiba.meta.xstream.converter;

import java.util.Locale;

import org.obiba.meta.Attribute;
import org.obiba.meta.ValueType;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class AttributeConverter implements Converter {

  public AttributeConverter() {
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type) {
    return Attribute.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Attribute variable = (Attribute) source;
    writer.addAttribute("name", variable.getName());
    writer.addAttribute("valueType", variable.getValueType().getName());
    if(variable.isLocalised()) {
      writer.addAttribute("locale", variable.getLocale().getLanguage());
    }
    writer.startNode("value");
    writer.setValue(variable.getValue().toString());
    writer.endNode();
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

    String name = reader.getAttribute("name");
    ValueType valueType = ValueType.Factory.forName(reader.getAttribute("valueType"));
    String locale = reader.getAttribute("locale");

    reader.moveDown();
    String valueString = reader.getValue();
    reader.moveUp();

    if(locale == null) {
      return Attribute.Builder.newAttribute(name).withValue(valueType.valueOf(valueString)).build();
    }
    return Attribute.Builder.newAttribute(name).withValue(new Locale(locale), valueString).build();
  }
}
