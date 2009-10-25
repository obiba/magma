package org.obiba.meta.type;

import java.lang.ref.WeakReference;
import java.math.BigInteger;

import javax.xml.namespace.QName;

import org.obiba.meta.MetaEngine;

public class IntegerType extends AbstractNumberType {

  private static final long serialVersionUID = 2345566305016760446L;

  private static final WeakReference<IntegerType> instance = MetaEngine.get().registerInstance(new IntegerType());

  private IntegerType() {

  }

  public static IntegerType get() {
    return instance.get();
  }

  @Override
  public Class<?> getJavaClass() {
    return BigInteger.class;
  }

  @Override
  public String getName() {
    return "integer";
  }

  @Override
  public QName getXsdType() {
    return new QName("xsd", "integer");
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz) || BigInteger.class.isAssignableFrom(clazz);
  }
}
