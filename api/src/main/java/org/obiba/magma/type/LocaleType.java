package org.obiba.magma.type;

import java.lang.ref.WeakReference;
import java.util.Locale;

import javax.annotation.Nullable;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;

public class LocaleType extends AbstractValueType {

  private static final long serialVersionUID = 6256436421177197681L;

  private static WeakReference<LocaleType> instance;

  private LocaleType() {

  }

  public static LocaleType get() {
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
  public boolean isDateTime() {
    return false;
  }

  @Override
  public boolean isNumeric() {
    return false;
  }

  @Override
  public Value valueOf(Object object) {
    if(object == null) {
      return nullValue();
    }
    Class<?> type = object.getClass();
    if(Locale.class.isAssignableFrom(type)) {
      return Factory.newValue(this, (Locale) object);
    }
    if(String.class.isAssignableFrom(type)) {
      return valueOf((String) object);
    }
    throw new IllegalArgumentException("Cannot construct " + getClass().getSimpleName() + " from type " + type + ".");
  }

  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    String parts[] = string.split("_");
    Locale locale;
    switch(parts.length) {
      case 1:
        locale = new Locale(parts[0]);
        break;
      case 2:
        locale = new Locale(parts[0], parts[1]);
        break;
      case 3:
        locale = new Locale(parts[0], parts[1], parts[2]);
        break;
      default:
        throw new IllegalArgumentException("Invalid locale string " + string);
    }
    return Factory.newValue(this, locale);
  }

  @Override
  public int compare(Value o1, Value o2) {
    if(o1 == null) throw new NullPointerException();
    if(o2 == null) throw new NullPointerException();
    if(!o1.getValueType().equals(this)) throw new ClassCastException();
    if(!o2.getValueType().equals(this)) throw new ClassCastException();
    // All Locales are considered equal when sorting.
    return 0;
  }
}
