/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.magma.Coordinate;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;

public class PointType extends JSONAwareValueType {

  private static final long serialVersionUID = 861524989622106076L;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<PointType> instance;

  private PointType() {

  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @Nonnull
  public static PointType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new PointType());
    }
    return instance.get();
  }

  @Nonnull
  @Override
  public String getName() {
    return "point";
  }

  @Override
  public Class<?> getJavaClass() {
    return Coordinate.class;
  }

  @Override
  public boolean acceptsJavaClass(@Nonnull Class<?> clazz) {
    return Coordinate.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean isDateTime() {
    return false;
  }

  @Override
  public boolean isNumeric() {
    return false;
  }

  @Nonnull
  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    return Factory.newValue(this, Coordinate.valueOf(string));
  }

  @Nonnull
  @Override
  public Value valueOf(@Nullable Object object) {
    if(object == null) {
      return nullValue();
    }

    Class<?> type = object.getClass();

    if(type.equals(Coordinate.class)) {
      return Factory.newValue(this, (Serializable) object);
    }
    if(type.equals(String.class)) {
      return valueOf((String) object);
    }
    if(type.equals(JSONArray.class)) {
      return valueOf(object.toString());
    }
    if(type.equals(JSONObject.class)) {
      return valueOf(object.toString());
    }
    throw new IllegalArgumentException(
        "Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }

  @Override
  public int compare(Value o1, Value o2) {
    return ((Coordinate) o1.getValue()).compareTo((Coordinate) o2.getValue());
  }

  @Nullable
  @Override
  protected String toString(@Nullable ValueSequence sequence) {
    return "[" + super.toString(sequence) + "]";
  }

}
