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
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_LOAD_OF_KNOWN_NULL_VALUE", justification = "In case of null argument, fall back to default behaviour")
  public String serializedClass(Class type) {
    if(type == null) return super.serializedClass(type);
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
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_LOAD_OF_KNOWN_NULL_VALUE", justification = "In case of null argument, fall back to default behaviour")
  public Class realClass(String elementName) {
    if(elementName == null) return super.realClass(elementName);
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
