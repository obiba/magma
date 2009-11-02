package org.obiba.meta.xstream.converter;

import org.obiba.meta.Attribute;
import org.obiba.meta.Category;
import org.obiba.meta.ValueType;
import org.obiba.meta.Variable;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class VariableConverter extends AbstractCollectionConverter {

  public VariableConverter(Mapper mapper) {
    super(new MapperWrapper(mapper) {

      @Override
      @SuppressWarnings("unchecked")
      public String serializedClass(Class type) {
        if(Attribute.class.isAssignableFrom(type)) {
          return "attribute";
        }
        if(Category.class.isAssignableFrom(type)) {
          return "category";
        }
        return super.serializedClass(type);
      }

      @Override
      @SuppressWarnings("unchecked")
      public Class realClass(String elementName) {
        if("attribute".equals(elementName)) {
          return Attribute.class;
        }
        if("category".equals(elementName)) {
          return Category.class;
        }
        return super.realClass(elementName);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type) {
    return Variable.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Variable variable = (Variable) source;
    writer.addAttribute("name", variable.getName());
    writer.addAttribute("collection", variable.getCollection());
    writer.addAttribute("valueType", variable.getValueType().getName());
    writer.addAttribute("entityType", variable.getEntityType());
    marshallAttributes(variable, writer, context);
    marshallCategories(variable, writer, context);
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Variable.Builder builder = Variable.Builder.newVariable(reader.getAttribute("collection"), reader.getAttribute("name"), ValueType.Factory.forName(reader.getAttribute("valueType")), reader.getAttribute("entityType"));

    if(reader.hasMoreChildren()) {
      reader.moveDown();
      if("attributes".equals(reader.getNodeName())) {
        while(reader.hasMoreChildren()) {
          Attribute attribute = readChildItem(reader, context, builder);
          builder.addAttribute(attribute);
        }
      } else if("categories".equals(reader.getNodeName())) {
        while(reader.hasMoreChildren()) {
          Category category = readChildItem(reader, context, builder);
          builder.addCategory(category);
        }
      }
      reader.moveUp();
    }

    return builder.build();
  }

  protected void marshallAttributes(Variable variable, HierarchicalStreamWriter writer, MarshallingContext context) {
    if(variable.hasAttributes()) {
      writer.startNode("attributes");
      for(Attribute a : variable.getAttributes()) {
        writeItem(a, context, writer);
      }
      writer.endNode();
    }
  }

  protected void marshallCategories(Variable variable, HierarchicalStreamWriter writer, MarshallingContext context) {
    if(variable.hasCategories()) {
      writer.startNode("categories");
      for(Category c : variable.getCategories()) {
        writeItem(c, context, writer);
      }
      writer.endNode();
    }
  }

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
