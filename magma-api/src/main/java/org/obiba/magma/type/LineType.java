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

public class LineType extends JSONAwareValueType {

  private static final long serialVersionUID = 1415659902603617833L;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<LineType> instance;

  private LineType() {

  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @Nonnull
  public static LineType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new LineType());
    }
    return instance.get();
  }

  @Nonnull
  @Override
  public String getName() {
    return "line";
  }

  @Override
  public Class<?> getJavaClass() {
    List<Coordinate> line;
    line = new ArrayList<Coordinate>();
    return line.getClass();
  }

  @Override
  public boolean acceptsJavaClass(@Nonnull Class<?> clazz) {
    List<Coordinate> line;
    line = new ArrayList<Coordinate>();
    return line.getClass().isAssignableFrom(clazz);
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
    try {
      JSONArray array = new JSONArray(string.trim());
      return valueOf(array);
    } catch(JSONException e) {
      throw new MagmaRuntimeException("Invalid Line format", e);
    }
  }

  public Value valueOf(@Nullable JSONArray array) {
    if(array == null) {
      return nullValue();
    }

    Collection<Coordinate> line = new ArrayList<Coordinate>(array.length());
    for(int i = 0; i < array.length(); i++) {
      try {
        line.add(Coordinate.getCoordinateFrom(array.get(i)));
      } catch(JSONException e) {
        throw new MagmaRuntimeException("Invalid Line format", e);
      }
    }
    if(line.isEmpty()) {
      throw new MagmaRuntimeException("The Line can't be empty");
    }
    return Factory.newValue(this, (Serializable) line);
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
      Collection<Coordinate> line = new ArrayList<Coordinate>();
      for(Object o : (List<?>) object) {
        line.add(Coordinate.getCoordinateFrom(o));
      }
      if(line.isEmpty()) {
        throw new MagmaRuntimeException("The Line can't be empty");
      }
      return Factory.newValue(this, (Serializable) line);
    }
    throw new IllegalArgumentException(
        "Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }

  @SuppressWarnings("unchecked")
  @Override
  public int compare(Value o1, Value o2) {
    Iterable<Coordinate> line1 = (Iterable<Coordinate>) o1.getValue();
    Iterable<Coordinate> line2 = (Iterable<Coordinate>) o2.getValue();

    if(Iterables.size(line1) == Iterables.size(line2)) {
      if(Iterables.elementsEqual(line1, line2)) return 0;
      return -1;
    }
    if(Iterables.size(line1) < Iterables.size(line2)) {
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
