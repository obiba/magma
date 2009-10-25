package org.obiba.meta.type;

import javax.xml.namespace.QName;

import org.obiba.meta.ValueType;

public class ValueSetReferenceType implements ValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  public static final ValueSetReferenceType INSTANCE = new ValueSetReferenceType();

  private ValueSetReferenceType() {

  }

  @Override
  public String getName() {
    return "valueSetReference";
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    // TODO Auto-generated method stub
    return false;
  }

  public Class<?> getJavaClass() {
    return String.class;
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
