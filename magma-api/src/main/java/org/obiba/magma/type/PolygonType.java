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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.obiba.magma.Coordinate;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;

public class PolygonType extends JSONAwareValueType {

  private static final long serialVersionUID = 1175515418625286891L;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<PolygonType> instance;

  private PolygonType() {

  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @Nonnull
  public static PolygonType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new PolygonType());
    }
    return instance.get();
  }

  @Nonnull
  @Override
  public String getName() {
    return "Polygon";
  }

  @Override
  public Class<?> getJavaClass() {
    List<List<Coordinate>> polygon = new ArrayList<List<Coordinate>>();
    return polygon.getClass();
  }

  @Override
  public boolean acceptsJavaClass(@Nonnull Class<?> clazz) {
    List<List<Coordinate>> polygon = new ArrayList<List<Coordinate>>();
    return polygon.getClass().isAssignableFrom(clazz);
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

    List<List<Coordinate>> polygon;
    JSONArray array;

    try {
      array = new JSONArray(string.trim());
      polygon = new ArrayList<List<Coordinate>>(array.length());

      for(int i = 0; i < array.length(); i++) {
        polygon.add(parsePolygon(array.getJSONArray(i)));
      }
    } catch(JSONException e) {
      throw new MagmaRuntimeException("Invalid Polygon format", e);
    }
    return Factory.newValue(this, (Serializable) polygon);
  }

  private List<Coordinate> parsePolygon(JSONArray array) {

    List<Coordinate> points = new ArrayList<Coordinate>(array.length());
    Coordinate coordinate;

    try {

      for(int i = 0; i < array.length(); i++) {
        coordinate = Coordinate.valueOf(array.getString(i));
        points.add(coordinate);
      }

      if(points.isEmpty()) {
        throw new MagmaRuntimeException("The polygon can't be empty");
      } else if(!(points.get(0).compareTo(points.get(array.length() - 1)) == 0)) {
        throw new MagmaRuntimeException("The first and the last point in each list must be the same");
      }

    } catch(JSONException e) {
      throw new MagmaRuntimeException("Invalid coordinate format", e);
    }
    return points;
  }

  @Nonnull
  @Override
  public Value valueOf(@Nullable Object object) {

    if(object == null) {
      return nullValue();
    }

    Class<?> type = object.getClass();

    if(type.equals(this.getJavaClass())) {

      if(((List) object).isEmpty()) {
        throw new MagmaRuntimeException("A polygon can't be empty");
      }

      List<List<Coordinate>> polygon = new ArrayList<List<Coordinate>>();
      List<Coordinate> pts = new ArrayList<Coordinate>();

      for(Object l : (List) object) {
        try {
          pts = getCoordinatesList((List) l);
        } catch(ClassCastException e) {
          throw new MagmaRuntimeException("List of Coordinates expected", e);
        }
        polygon.add(pts);
      }
      return Factory.newValue(this, (Serializable) polygon);
    }

    if(type.equals(String.class)) {
      return valueOf((String) object);
    }
    throw new IllegalArgumentException(
        "Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }

  private List<Coordinate> getCoordinatesList(List<?> object) {

    List<Coordinate> points = new ArrayList<Coordinate>();

    for(Object o : object) {
      if(o.getClass().equals(Coordinate.class)) {
        points.add((Coordinate) o);
      } else if(o.getClass().equals(String.class)) {
        points.add(Coordinate.valueOf((String) o));
      } else throw new IllegalArgumentException("Cannot construct a polygon from this class");
    }
    if(points.isEmpty()) {
      throw new NullPointerException("A polygon can't be empty");
    }

    return points;
  }

  @Override
  public int compare(Value o1, Value o2) {
    List<List<Coordinate>> l1 = (List<List<Coordinate>>) o1.getValue();
    List<List<Coordinate>> l2 = (List<List<Coordinate>>) o2.getValue();

    if(l1.size() == l2.size()) {

      for(List<Coordinate> l : l1) {
        if(!comparePoints(l, l2)) return -1;
      }
      return 0;
    }
    if(l1.size() < l2.size()) {
      return -1;
    }
    return 1;
  }

  @Nullable
  @Override
  protected String toString(@Nullable ValueSequence sequence) {
    return "[" + super.toString(sequence) + "]";
  }

  private boolean comparePoints(Collection<Coordinate> l1, Iterable<List<Coordinate>> polygon) {
    for(List<Coordinate> list : polygon) {
      if(l1.containsAll(list) && l1.size() == list.size()) return true;
    }
    return false;
  }
}
