package org.obiba.meta.type;

import java.lang.ref.WeakReference;
import java.util.Locale;

import org.obiba.meta.MetaEngine;

public class LocaleType extends TextType {

  private static final long serialVersionUID = 6256436421177197681L;

  private static WeakReference<LocaleType> instance;

  private LocaleType() {

  }

  @Override
  public String getName() {
    return "locale";
  }

  public static TextType get() {
    if(instance == null || instance.get() == null) {
      instance = MetaEngine.get().registerInstance(new LocaleType());
    }
    return instance.get();
  }

  public Class<?> getJavaClass() {
    return Locale.class;
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return Locale.class.isAssignableFrom(clazz);
  }

}
