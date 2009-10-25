package org.obiba.meta.type;

import javax.xml.namespace.QName;

import org.obiba.meta.ValueType;

public class BinaryType implements ValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  public static final BinaryType INSTANCE = new BinaryType();

  private BinaryType() {

  }

  @Override
  public String getName() {
    return "binary";
  }

  public Class<?> getJavaClass() {
    return byte[].class;
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return byte[].class.isAssignableFrom(clazz);
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
