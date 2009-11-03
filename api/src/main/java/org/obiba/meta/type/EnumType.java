package org.obiba.meta.type;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import javax.xml.namespace.QName;

import org.obiba.meta.MetaEngine;
import org.obiba.meta.Value;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;

public class EnumType implements EnumeratedType {

  private static final long serialVersionUID = -5271259966499174607L;

  private static WeakReference<EnumType> instance;

  private EnumType() {

  }

  public static EnumType get() {
    if(instance == null) {
      instance = MetaEngine.get().registerInstance(new EnumType());
    }
    return instance.get();
  }

  @Override
  public String getName() {
    return "text-enumerated";
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

  public String[] enumerate(Class<?> enumClass) {
    if(enumClass.isEnum()) {
      Enum<?>[] enums = (Enum<?>[]) enumClass.getEnumConstants();
      return Iterables.toArray(Iterables.transform(Arrays.asList(enums), Functions.toStringFunction()), String.class);
    }
    throw new IllegalArgumentException(enumClass.getName() + " is not an enum.");
  }

  @Override
  public String toString(Value value) {
    return value.isNull() ? null : value.getValue().toString();
  }

  @Override
  public Value valueOf(String string) {
    // Since we don't know what enum type we represent, we cannot use the proper enum constant. We can only use its
    // String representation
    return Factory.newValue(this, string);
  }

  @Override
  public Value valueOf(Object object) {
    if(object == null) {
      return Factory.newValue(this, null);
    }
    Class<?> type = object.getClass();
    if(type.isEnum()) {
      return Factory.newValue(this, object.toString());
    }
    throw new IllegalArgumentException("Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }

}
