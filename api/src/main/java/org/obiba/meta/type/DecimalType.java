package org.obiba.meta.type;

import java.math.BigDecimal;

import javax.xml.namespace.QName;

public class DecimalType extends AbstractNumberType {

  public static final DecimalType INSTANCE = new DecimalType();

  private DecimalType() {

  }

  @Override
  public Class<?> getJavaClass() {
    return BigDecimal.class;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public QName getXsdType() {
    return null;
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return Double.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz);
  }
}
