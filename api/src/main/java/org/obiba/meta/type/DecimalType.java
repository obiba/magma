package org.obiba.meta.type;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

import javax.xml.namespace.QName;

import org.obiba.meta.MetaEngine;

public class DecimalType extends AbstractNumberType {

  private static final long serialVersionUID = -149385659514790222L;

  private static WeakReference<DecimalType> instance;

  private DecimalType() {

  }

  public static DecimalType get() {
    if(instance == null) {
      instance = MetaEngine.get().registerInstance(new DecimalType());
    }
    return instance.get();
  }

  @Override
  public Class<?> getJavaClass() {
    return BigDecimal.class;
  }

  @Override
  public String getName() {
    return "decimal";
  }

  @Override
  public QName getXsdType() {
    return new QName("xsd", "decimal");
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz) || BigDecimal.class.isAssignableFrom(clazz);
  }
}
