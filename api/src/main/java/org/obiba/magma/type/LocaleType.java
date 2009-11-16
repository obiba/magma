package org.obiba.magma.type;

import java.lang.ref.WeakReference;
import java.util.Locale;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;

public class LocaleType extends TextType {

  private static final long serialVersionUID = 6256436421177197681L;

  private static WeakReference<LocaleType> instance;

  private LocaleType() {

  }

  public static TextType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new LocaleType());
    }
    return instance.get();
  }

  @Override
  public String getName() {
    return "locale";
  }

  public Class<?> getJavaClass() {
    return Locale.class;
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return Locale.class.isAssignableFrom(clazz);
  }

  @Override
  public Value valueOf(Object object) {
    if(object == null) {
      return nullValue();
    }
    if(Locale.class.isAssignableFrom(object.getClass())) {
      return Factory.newValue(this, (Locale) object);
    }
    throw new IllegalArgumentException("Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }

  @Override
  public Value valueOf(String string) {
    if(string == null) {
      return nullValue();
    }
    return Factory.newValue(this, new Locale(string));
  }
}
