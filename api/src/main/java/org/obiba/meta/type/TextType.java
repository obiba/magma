package org.obiba.meta.type;

import javax.xml.namespace.QName;

import org.obiba.meta.ValueType;

public class TextType implements ValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  public static final TextType INSTANCE = new TextType();

  private TextType() {

  }

  @Override
  public String getName() {
    return "text";
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
