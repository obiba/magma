package org.obiba.meta.type;

import javax.xml.namespace.QName;

import org.obiba.meta.ValueType;

public class EnumType implements ValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  public static final EnumType INSTANCE = new EnumType();

  private EnumType() {

  }

  @Override
  public String getName() {
    return "enumerated";
  }

  public Class<?> getJavaClass() {
    return Enum.class;
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return clazz.isEnum();
  }

  public QName getXsdType() {
    return new QName("xsd", "string");
  }

  public boolean isDateTime() {
    return false;
  }

  public boolean isNumeric() {
    return false;
  }

}
