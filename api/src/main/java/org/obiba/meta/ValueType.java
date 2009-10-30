package org.obiba.meta;

import java.io.Serializable;

import javax.xml.namespace.QName;

public interface ValueType extends Serializable {

  public String getName();

  public QName getXsdType();

  public Class<?> getJavaClass();

  public boolean acceptsJavaClass(Class<?> clazz);

  public boolean isDateTime();

  public boolean isNumeric();

  public String toString(Value value);
}
