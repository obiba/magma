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

import com.google.common.collect.Iterables;

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
    return "polygon";
  }

  @Override
  public Class<?> getJavaClass() {
    List<List<Coordinate>> polygon;
    polygon = new ArrayList<List<Coordinate>>();
    return polygon.getClass();
  }

  @Override
  public boolean acceptsJavaClass(@Nonnull Class<?> clazz) {
    List<List<Coordinate>> polygon;
    polygon = new ArrayList<List<Coordinate>>();
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

  @Override
  public boolean isGeo() {
    return true;
  }

  @Nonnull
  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }

    try {
      JSONArray array = new JSONArray(string.trim());
      return valueOf(array);
    } catch(JSONException e) {
      throw new MagmaRuntimeException("Invalid Polygon format", e);
    }
  }

  public Value valueOf(@Nullable JSONArray array) {
    if(array == null) {
      return nullValue();
    }

    Collection<List<Coordinate>> polygon = new ArrayList<List<Coordinate>>(array.length());

    for(int i = 0; i < array.length(); i++) {
      try {
        List<Coordinate> points = new ArrayList<Coordinate>(array.getJSONArray(i).length());
        for(int j = 0; j < array.getJSONArray(i).length(); j++) {
          points.add(Coordinate.getCoordinateFrom(array.getJSONArray(i).get(j)));
        }
        if(points.isEmpty()) {
          throw new MagmaRuntimeException("The Polygon can't be empty");
        }
        if(!(points.get(0).compareTo(points.get(points.size() - 1)) == 0)) {
          throw new MagmaRuntimeException("The last Coordinate must be the same as the first coordinate");
        }
        polygon.add(points);
      } catch(JSONException e) {
        throw new MagmaRuntimeException("Invalid Polygon format");
      }
    }
    return Factory.newValue(this, (Serializable) polygon);
  }

  @Nonnull
  @Override
  public Value valueOf(@Nullable Object object) {

    if(object == null) {
      return nullValue();
    }

    Class<?> type = object.getClass();
    if(type.equals(String.class)) {
      return valueOf((String) object);
    }
    if(type.equals(JSONArray.class)) {
      return valueOf((JSONArray) object);
    }
    if(type.equals(getJavaClass())) {
      Collection<List<Coordinate>> polygon = new ArrayList<List<Coordinate>>();
      List<Coordinate> pts = new ArrayList<Coordinate>();

      for(Object obj : (List<?>) object) {
        try {
          for(Object o : (Iterable<?>) obj) {
            pts.add(Coordinate.getCoordinateFrom(o));
          }
        } catch(ClassCastException e) {
          throw new MagmaRuntimeException("List of Coordinates expected", e);
        }
      }
      if(pts.isEmpty()) {
        throw new MagmaRuntimeException("A polygon can't be empty");
      }
      polygon.add(pts);
      return Factory.newValue(this, (Serializable) polygon);
    }
    throw new IllegalArgumentException(
        "Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }

  @SuppressWarnings("unchecked")
  @Override
  public int compare(Value o1, Value o2) {
    Iterable<List<Coordinate>> list1 = (Iterable<List<Coordinate>>) o1.getValue();
    Iterable<List<Coordinate>> list2 = (Iterable<List<Coordinate>>) o2.getValue();

    if(list1 == list2) return 0;
    if(list1 == null) return -1;
    if(list2 == null) return 1;

    int size1 = Iterables.size(list1);
    int size2 = Iterables.size(list2);
    if(size1 == size2) {
      for(List<Coordinate> l : list1) {
        if(!Iterables.contains(list2, l)) return -1;
      }
      return 0;
    }
    if(size1 < size2) {
      return -1;
    }
    return 1;
  }

  @Nullable
  @Override
  protected String toString(@Nullable ValueSequence sequence) {
    return "[" + super.toString(sequence) + "]";
  }

}
