package org.obiba.magma.xstream.mapper;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.Variable;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class MagmaMapper extends MapperWrapper {

  public MagmaMapper(Mapper wrapped) {
    super(wrapped);
  }

  @Override
  @SuppressWarnings("unchecked")
  public String serializedClass(Class type) {
    if(Variable.class.isAssignableFrom(type)) {
      return "variable";
    }
    if(Attribute.class.isAssignableFrom(type)) {
      return "attribute";
    }
    if(Category.class.isAssignableFrom(type)) {
      return "category";
    }
    if(ValueSequence.class.equals(type)) {
      return "sequence";
    }
    return super.serializedClass(type);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class realClass(String elementName) {
    if("variable".equals(elementName)) {
      return Variable.class;
    }
    if("attribute".equals(elementName)) {
      return Attribute.class;
    }
    if("category".equals(elementName)) {
      return Category.class;
    }
    if("sequence".equals(elementName)) {
      return ValueSequence.class;
    }
    return super.realClass(elementName);
  }

}
