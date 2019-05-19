/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
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

  @NotNull
  private Value value;

  private String unit;

  /**
   * No-arg ctor for building the prototype
   */
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
      justification = "Needed by ScriptableValuePrototypeFactory")
  ScriptableValue() {

  }

  public ScriptableValue(Scriptable scope, @NotNull Value value, @Nullable String unit) {
    super(scope, ScriptableObject.getClassPrototype(scope, VALUE_CLASS_NAME));
    //noinspection ConstantConditions
    if(value == null) throw new IllegalArgumentException("value cannot be null");
    this.value = value;
    this.unit = unit;
  }

  public ScriptableValue(Scriptable scope, @NotNull Value value) {
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
    ValueType valueType = value.getValueType();
    boolean isNull = value.isNull();
    if(valueType.isDateTime()) {
      if(isNull) {
        return Context.toObject(null, this);
      }
      double jsDate = valueType == DateType.get()
          ? ((MagmaDate) value.getValue()).asDate().getTime()
          : ((Date) value.getValue()).getTime();
      return Context.toObject(ScriptRuntime.wrapNumber(jsDate), this);
    }
    if(valueType.isGeo()) {
      return isNull ? Context.toObject(null, this) : getGeoDefaultValue(valueType, value.getValue());
    }
    if(valueType.isNumeric()) {
      return Context.toNumber(isNull ? Undefined.instance : value.getValue());
    }
    if(valueType.equals(BooleanType.get())) {
      return Context.toBoolean(isNull ? null : value.getValue());
    }
    if(valueType.equals(TextType.get())) {
      return Context.toString(isNull ? null : value.getValue());
    }
    return value.getValue();
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

  @NotNull
  public Value getValue() {
    return value;
  }

  public ValueType getValueType() {
    return getValue().getValueType();
  }

  /**
   * Check if the value contains or is equal to (if not a sequence) the given value.
   *
   * @param testValue
   * @return
   */
  public boolean contains(Value testValue) {
    return getValue().isSequence() //
        ? getValue().asSequence().contains(testValue) //
        : getValue().equals(testValue);
  }

  /**
   * Get the position of the given value in the sequence or 0 if not a sequence.
   *
   * @param testValue
   * @return -1 if not found
   */
  public int indexOf(Value testValue) {
    return getValue().isSequence() //
        ? getValue().asSequence().indexOf(testValue) //
        : getValue().equals(testValue) ? 0 : -1;
  }

  /**
   * Get the last position of the given value in the sequence or 0 if not a sequence.
   *
   * @param testValue
   * @return -1 if not found
   */
  public int lastIndexOf(Value testValue) {
    return getValue().isSequence() //
        ? getValue().asSequence().lastIndexOf(testValue) //
        : getValue().equals(testValue) ? 0 : -1;
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
