package org.obiba.meta.xstream.converter;

import org.obiba.meta.Category;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CategoryConverter implements Converter {

  public CategoryConverter() {
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type) {
    return Category.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Category category = (Category) source;
    writer.addAttribute("name", category.getName());
    writer.addAttribute("code", category.getCode());
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String name = reader.getAttribute("name");
    String code = reader.getAttribute("code");
    return Category.Builder.newCategory(name).withCode(code).build();
  }
}
