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
import java.math.BigInteger;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;

import com.google.common.base.Strings;

public class IntegerType extends AbstractNumberType {

  private static final long serialVersionUID = 2345566305016760446L;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<IntegerType> instance;

  private IntegerType() {

  }

  @SuppressWarnings("ConstantConditions")
  @NotNull
  public static IntegerType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new IntegerType());
    }
    return instance.get();
  }

  @Override
  public Class<?> getJavaClass() {
    return Long.class;
  }

  @NotNull
  @Override
  public String getName() {
    return "integer";
  }

  @Override
  public boolean acceptsJavaClass(@NotNull Class<?> clazz) {
    return Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz) ||
        Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz) ||
        BigInteger.class.isAssignableFrom(clazz);
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable String string) {
    try {
      return Strings.isNullOrEmpty(string) ? nullValue() : Factory.newValue(this, isScientificNotation(string) ? parseDoubleAsLong(string) : parseLong(string));
    } catch(NumberFormatException e) {
      throw new MagmaRuntimeException("Not a integer value: " + string, e);
    }
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable Object object) {
    if(object == null) {
      return nullValue();
    }
    Class<?> type = object.getClass();
    if(Number.class.isAssignableFrom(type)) {
      return Factory.newValue(this, ((Number) object).longValue());
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

  private boolean isScientificNotation(String string) {
    return !Strings.isNullOrEmpty(string) && string.toUpperCase().contains("E");
  }

  private Long parseDoubleAsLong(String string) {
    return Double.valueOf(normalize(string)).longValue();
  }

  private Long parseLong(String string) {
    return Long.valueOf(normalize(string));
  }

  private String normalize(String string) {
    String rval = string.replace(",", ".").trim();
    if (rval.length()>2 && rval.endsWith(".0")) return rval.substring(0, rval.length()-2);
    return rval;
  }

}
