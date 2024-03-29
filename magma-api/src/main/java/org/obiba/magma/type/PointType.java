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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.json.JSONObject;
import org.obiba.magma.Coordinate;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.support.ValueComparator;

public class PointType extends JSONAwareValueType {

  private static final long serialVersionUID = 861524989622106076L;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<PointType> instance;

  private PointType() {

  }

  @SuppressWarnings("ConstantConditions")
  @NotNull
  public static PointType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new PointType());
    }
    return instance.get();
  }

  @NotNull
  @Override
  public String getName() {
    return "point";
  }

  @Override
  public Class<?> getJavaClass() {
    return Coordinate.class;
  }

  @Override
  public boolean acceptsJavaClass(@NotNull Class<?> clazz) {
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

  @Override
  public boolean isGeo() {
    return true;
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    return Factory.newValue(this, Coordinate.getCoordinateFrom(string));
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable Object object) {
    if(object == null || object.equals(JSONObject.NULL)) {
      return nullValue();
    }
    return Factory.newValue(this, Coordinate.getCoordinateFrom(object));
  }

  @Override
  public int compare(Value o1, Value o2) {
    return ValueComparator.INSTANCE.compare(o1, o2);
  }

  @Nullable
  @Override
  protected String toString(@Nullable ValueSequence sequence) {
    return "[" + super.toString(sequence) + "]";
  }

}
