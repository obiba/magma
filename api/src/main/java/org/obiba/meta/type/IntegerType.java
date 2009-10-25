package org.obiba.meta.type;

import java.math.BigInteger;

import javax.xml.namespace.QName;

public class IntegerType extends AbstractNumberType {

  private static final long serialVersionUID = 2345566305016760446L;

  public static final IntegerType INSTANCE = new IntegerType();

  private IntegerType() {

  }

  @Override
  public Class<?> getJavaClass() {
    return BigInteger.class;
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
    return Integer.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz) || BigInteger.class.isAssignableFrom(clazz);
  }
}
