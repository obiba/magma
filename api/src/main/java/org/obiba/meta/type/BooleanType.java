package org.obiba.meta.type;

import java.lang.ref.WeakReference;

import javax.xml.namespace.QName;

import org.obiba.meta.MetaEngine;
import org.obiba.meta.Value;
import org.obiba.meta.ValueType;

public class BooleanType implements ValueType {

  private static final long serialVersionUID = -149385659514790222L;

  private static WeakReference<BooleanType> instance;

  private BooleanType() {

  }

  public static BooleanType get() {
    if(instance == null) {
      instance = MetaEngine.get().registerInstance(new BooleanType());
    }
    return instance.get();
  }

  @Override
  public boolean isDateTime() {
    return false;
  }

  @Override
  public boolean isNumeric() {
    return false;
  }

  @Override
  public Class<?> getJavaClass() {
    return Boolean.class;
  }

  @Override
  public String getName() {
    return "boolean";
  }

  @Override
  public QName getXsdType() {
    return new QName("xsd", "boolean");
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz);
  }

  @Override
  public String toString(Value value) {
    return value.isNull() ? null : value.getValue().toString();
  }

  @Override
  public Value valueOf(String string) {
    return Factory.newValue(this, Boolean.valueOf(string));
  }

  @Override
  public Value valueOf(Object object) {
    String str = object != null ? object.toString() : null;
    return Factory.newValue(this, Boolean.valueOf(str));
  }
}
