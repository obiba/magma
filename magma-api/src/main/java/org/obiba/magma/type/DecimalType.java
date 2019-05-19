/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;

public class DecimalType extends AbstractNumberType {

  private static final long serialVersionUID = -149385659514790222L;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<DecimalType> instance;

  private DecimalType() {

  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @NotNull
  public static DecimalType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new DecimalType());
    }
    return instance.get();
  }

  @Override
  public Class<?> getJavaClass() {
    return Double.class;
  }

  @NotNull
  @Override
  public String getName() {
    return "decimal";
  }

  @Override
  public boolean acceptsJavaClass(@NotNull Class<?> clazz) {
    return Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz) ||
        Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz) ||
        BigDecimal.class.isAssignableFrom(clazz);
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    try {
      return Factory.newValue(this, Double.valueOf(normalize(string)));
    } catch(NumberFormatException e) {
      throw new MagmaRuntimeException("Not a decimal value: " + string, e);
    }
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable Object object) {
    if(object == null) {
      return nullValue();
    }
    Class<?> type = object.getClass();
    if (Float.class.isAssignableFrom(type)) {
      Float floatValue = (Float) object;
      // required to handle extra precision of doubles compared to floats.
      return Factory.newValue(this, Double.valueOf(floatValue.toString()));
    }
    if(Number.class.isAssignableFrom(type)) {
      return Factory.newValue(this, ((Number) object).doubleValue());
    }
    if(String.class.isAssignableFrom(type)) {
      return valueOf((String) object);
    }
    if(object instanceof Value) {
      return convert((Value)object);
    }
    throw new IllegalArgumentException(
        "Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }

  private String normalize(String string) {
    return string.replace(",", ".").trim();
  }
}
