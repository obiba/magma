package org.obiba.meta.xstream.converter;

import org.obiba.meta.Attribute;
import org.obiba.meta.AttributeAware;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public abstract class AbstractAttributeAwareConverter extends AbstractCollectionConverter {

  public AbstractAttributeAwareConverter(Mapper mapper) {
    super(new MapperWrapper(mapper) {

      @Override
      @SuppressWarnings("unchecked")
      public String serializedClass(Class type) {
        if(Attribute.class.isAssignableFrom(type)) {
          return "attribute";
        }

        return super.serializedClass(type);
      }

      @Override
      @SuppressWarnings("unchecked")
      public Class realClass(String elementName) {
        if("attribute".equals(elementName)) {
          return Attribute.class;
        }

        return super.realClass(elementName);
      }
    });
  }

  protected void marshallAttributes(AttributeAware attributeAware, HierarchicalStreamWriter writer, MarshallingContext context) {
    if(attributeAware.hasAttributes()) {
      writer.startNode("attributes");
      for(Attribute a : attributeAware.getAttributes()) {
        writeItem(a, context, writer);
      }
      writer.endNode();
    }
  }

  protected void unmarshallAttributes(Object current, HierarchicalStreamReader reader, UnmarshallingContext context) {

    while(reader.hasMoreChildren()) {
      Attribute attribute = readChildItem(reader, context, current);
      addAttribute(current, attribute);
    }

  }

  protected boolean isAttributesNode(String nodeName) {
    return "attributes".equals(nodeName);
  }

  abstract void addAttribute(Object current, Attribute attribute);

  /**
   * Utility method for reading an item of a collection. It was created because the {@code readItem} method from the
   * parent class does not move the reader ({@code HierarchicalStreamReader#moveDown()} and{@code
   * HierarchicalStreamReader#moveUp()})
   */
  @SuppressWarnings("unchecked")
  protected <T> T readChildItem(HierarchicalStreamReader reader, UnmarshallingContext context, Object current) {
    reader.moveDown();
    T o = (T) super.readItem(reader, context, current);
    reader.moveUp();
    return o;
  }
}
