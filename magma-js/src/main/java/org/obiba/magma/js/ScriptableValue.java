package org.obiba.magma.js;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.Coordinate;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
import org.obiba.magma.type.TextType;

/**
 * A {@code Scriptable} implementation for {@code Value} objects.
 * <p/>
 * Methods available on the {@code ScriptableValue} instances are built by the {@code ScriptableValuePrototypeFactory}.
 * It allows extending the methods of {@code ScriptableValue}.
 *
 * @see ScriptableValuePrototypeFactory
 */
public class ScriptableValue extends ScriptableObject {

  private static final long serialVersionUID = -4342110775412157728L;

  static final String VALUE_CLASS_NAME = "Value";

  @Nonnull
  private Value value;

  private String unit;

  /**
   * No-arg ctor for building the prototype
   */
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
      justification = "Needed by ScriptableValuePrototypeFactory")
  ScriptableValue() {

  }

  public ScriptableValue(Scriptable scope, @Nonnull Value value, @Nullable String unit) {
    super(scope, ScriptableObject.getClassPrototype(scope, VALUE_CLASS_NAME));
    //noinspection ConstantConditions
    if(value == null) {
      throw new NullPointerException("value cannot be null");
    }
    this.value = value;
    this.unit = unit;
  }

  public ScriptableValue(Scriptable scope, @Nonnull Value value) {
    this(scope, value, null);
  }

  public boolean hasUnit() {
    return unit != null;
  }

  public String getUnit() {
    return unit;
  }

  @Override
  public String getClassName() {
    return VALUE_CLASS_NAME;
  }

  @Nullable
  @Override
  public Object getDefaultValue(Class<?> typeHint) {
    if(value.isSequence()) {
      return value.asSequence().toString();
    }
    Object defaultValue = value.getValue();
    if(value.isNull()) {
      return Context.toObject(defaultValue, this);
    }
    if(value.getValueType().isDateTime()) {
      double jsDate;
      //noinspection ConstantConditions
      jsDate = value.getValueType() == DateType.get()
          ? ((MagmaDate) defaultValue).asDate().getTime()
          : ((Date) defaultValue).getTime();
      return Context.toObject(ScriptRuntime.wrapNumber(jsDate), this);
    }
    if(value.getValueType().isGeo()) {
      return getGeoDefaultValue(value.getValueType(), defaultValue);
    }
    if(value.getValueType().isNumeric()) {
      return Context.toNumber(defaultValue);
    }
    if(value.getValueType().equals(BooleanType.get())) {
      return Context.toBoolean(defaultValue);
    }
    if(value.getValueType().equals(TextType.get())) {
      return Context.toString(defaultValue);
    }
    return defaultValue;
  }

  @SuppressWarnings("unchecked")
  private Object getGeoDefaultValue(ValueType type, Object defaultValue) {
    if(PointType.get().equals(type)) {
      return ((Coordinate) defaultValue).toArray();
    }
    if(LineStringType.get().equals(type)) {
      return getLineDefaultValue((Collection<Coordinate>) defaultValue);
    }
    if(PolygonType.get().equals(type)) {
      return getPolygonDefaultValue((Collection<List<Coordinate>>) defaultValue);
    }
    return defaultValue;
  }

  private double[][] getLineDefaultValue(Collection<Coordinate> line) {
    double[][] dline = new double[line.size()][];
    int i = 0;
    for(Coordinate coordinate : line) {
      dline[i++] = coordinate.toArray();
    }
    return dline;
  }

  private double[][][] getPolygonDefaultValue(Collection<List<Coordinate>> polygon) {
    double[][][] dpolygon = new double[polygon.size()][][];

    int i = 0;
    for(List<Coordinate> line : polygon) {
      dpolygon[i++] = getLineDefaultValue(line);
    }

    return dpolygon;
  }

  @Nonnull
  public Value getValue() {
    return value;
  }

  public ValueType getValueType() {
    return getValue().getValueType();
  }

  public boolean contains(Value testValue) {
    if(getValue().isSequence()) {
      return getValue().asSequence().contains(testValue);
    }
    return getValue().equals(testValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Nullable
  @Override
  public String toString() {
    return getValue().toString();
  }
}
