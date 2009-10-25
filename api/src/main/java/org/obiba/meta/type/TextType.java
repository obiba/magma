package org.obiba.meta.type;

import java.lang.ref.WeakReference;

import javax.xml.namespace.QName;

import org.obiba.meta.MetaEngine;
import org.obiba.meta.ValueType;

public class TextType implements ValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  private static final WeakReference<TextType> instance = MetaEngine.get().registerInstance(new TextType());

  private TextType() {

  }

  public static TextType get() {
    return instance.get();
  }

  @Override
  public String getName() {
    return "text";
  }

  public Class<?> getJavaClass() {
    return String.class;
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return String.class.isAssignableFrom(clazz);
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
