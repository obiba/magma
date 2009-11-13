package org.obiba.meta.type;

import java.lang.ref.WeakReference;

import javax.xml.namespace.QName;

import org.obiba.meta.MetaEngine;
import org.obiba.meta.Value;

public class TextType extends AbstractValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  private static WeakReference<TextType> instance;

  protected TextType() {

  }

  public static TextType get() {
    if(instance == null || instance.get() == null) {
      instance = MetaEngine.get().registerInstance(new TextType());
    }
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
    return String.class.isAssignableFrom(clazz) || clazz.isEnum();
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

  @Override
  public String toString(Value value) {
    return (String) value.getValue();
  }

  @Override
  public Value valueOf(String string) {
    return Factory.newValue(this, string);
  }

  @Override
  public Value valueOf(Object object) {
    return Factory.newValue(this, object != null ? object.toString() : null);
  }
}
