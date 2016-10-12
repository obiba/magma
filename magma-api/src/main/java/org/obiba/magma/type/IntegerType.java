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
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
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
      return Strings.isNullOrEmpty(string) ? nullValue() : Factory.newValue(this, Long.valueOf(normalize(string)));
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
    throw new IllegalArgumentException(
        "Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }

  private String normalize(String string) {
    return string.trim();
  }

}
