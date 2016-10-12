/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;

public class LocaleType extends AbstractValueType {

  private static final long serialVersionUID = 6256436421177197681L;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<LocaleType> instance;

  private LocaleType() {

  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @NotNull
  public static LocaleType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new LocaleType());
    }
    return instance.get();
  }

  @NotNull
  @Override
  public String getName() {
    return "locale";
  }

  @Override
  public Class<?> getJavaClass() {
    return Locale.class;
  }

  @Override
  public boolean acceptsJavaClass(@NotNull Class<?> clazz) {
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

  @NotNull
  @Override
  public Value valueOf(@Nullable Object object) {
    if(object == null) {
      return nullValue();
    }
    Class<?> type = object.getClass();
    if(Locale.class.isAssignableFrom(type)) {
      return Factory.newValue(this, (Serializable) object);
    }
    if(String.class.isAssignableFrom(type)) {
      return valueOf((String) object);
    }
    throw new IllegalArgumentException("Cannot construct " + getClass().getSimpleName() + " from type " + type + ".");
  }

  @NotNull
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
