package org.obiba.meta.type;

import java.lang.ref.WeakReference;

import javax.xml.namespace.QName;

import org.obiba.meta.MetaEngine;
import org.obiba.meta.ValueType;

public class EnumType implements ValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  private static final WeakReference<EnumType> instance = MetaEngine.get().registerInstance(new EnumType());

  private EnumType() {

  }

  public static EnumType get() {
    return instance.get();
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
