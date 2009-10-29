package org.obiba.meta.type;

import java.lang.ref.WeakReference;
import java.util.Date;

import javax.xml.namespace.QName;

import org.obiba.meta.MetaEngine;
import org.obiba.meta.Value;
import org.obiba.meta.ValueType;

public class DateType implements ValueType {

  private static final long serialVersionUID = -149385659514790222L;

  private static WeakReference<DateType> instance;

  private DateType() {

  }

  public static DateType get() {
    if(instance == null) {
      instance = MetaEngine.get().registerInstance(new DateType());
    }
    return instance.get();
  }

  @Override
  public boolean isDateTime() {
    return true;
  }

  @Override
  public boolean isNumeric() {
    return true;
  }

  @Override
  public Class<?> getJavaClass() {
    return Date.class;
  }

  @Override
  public String getName() {
    return "date";
  }

  @Override
  public QName getXsdType() {
    return new QName("xsd", "date");
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return Date.class.isAssignableFrom(clazz) || java.sql.Date.class.isAssignableFrom(clazz) || java.sql.Timestamp.class.isAssignableFrom(clazz);
  }

  @Override
  public String toString(Value value) {
    Date date = (Date) value.getValue();
    return date == null ? null : date.toString();
  }
}
