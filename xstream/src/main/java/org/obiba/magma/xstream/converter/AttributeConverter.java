package org.obiba.meta.xstream.converter;

import java.util.Locale;

import org.obiba.meta.Attribute;
import org.obiba.meta.ValueType;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts an {@code Attribute} instance.
 * <p>
 * Resulting XML:
 * 
 * <pre>
 * &lt;attribute name="attributeName" valueType="integer" locale="en"&gt;12345&lt;/attribute>
 * </pre>
 */
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
    writer.setValue(variable.getValue().toString());
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

    String name = reader.getAttribute("name");
    ValueType valueType = ValueType.Factory.forName(reader.getAttribute("valueType"));
    String locale = reader.getAttribute("locale");

    String valueString = reader.getValue();

    if(locale == null) {
      return Attribute.Builder.newAttribute(name).withValue(valueType.valueOf(valueString)).build();
    }
    return Attribute.Builder.newAttribute(name).withValue(new Locale(locale), valueString).build();
  }
}
