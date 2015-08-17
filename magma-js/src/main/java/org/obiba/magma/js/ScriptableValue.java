package org.obiba.magma.js;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Maps;
import jdk.nashorn.api.scripting.JSObject;
import org.obiba.magma.Coordinate;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.methods.BooleanMethods;
import org.obiba.magma.js.methods.CompareMethods;
import org.obiba.magma.js.methods.DateTimeMethods;
import org.obiba.magma.js.methods.GeoMethods;
import org.obiba.magma.js.methods.NumericMethods;
import org.obiba.magma.js.methods.ScriptableValueMethods;
import org.obiba.magma.js.methods.TextMethods;
import org.obiba.magma.js.methods.UnitMethods;
import org.obiba.magma.js.methods.ValueSequenceMethods;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
import org.obiba.magma.type.TextType;


/**
 * A {@code Scriptable} implementation for {@code Value} objects.
 * <p>
 * It allows extending the methods of {@code ScriptableValue}.
 */
public class ScriptableValue extends Scriptable {

  @NotNull
  private Value value;

  private String unit;

  public ScriptableValue(@NotNull Value value, @Nullable String unit) {
    if(value == null) throw new IllegalArgumentException("value cannot be null");
    this.value = value;
    this.unit = unit;
  }

  public ScriptableValue(@NotNull Value value) {
    this(value, null);
  }

  public boolean hasUnit() {
    return unit != null;
  }

  public String getUnit() {
    return unit;
  }

  @Nullable
  public Object getDefaultValue(Class<?> typeHint) {
    if(value.isSequence()) {
      return value.asSequence().toString();
    }

    ValueType valueType = value.getValueType();
    boolean isNull = value.isNull();

    if(valueType.isDateTime()) {

      if(isNull) {
        return null;
      }

      double jsDate = valueType == DateType.get()
          ? ((MagmaDate) value.getValue()).asDate().getTime()
          : ((Date) value.getValue()).getTime();

      return jsDate;
    }

    if(valueType.isGeo()) {
      return isNull ? null : getGeoDefaultValue(valueType, value.getValue());
    }

    if(valueType.isNumeric()) {
      return isNull ? null : value.getValue();
    }

    if(valueType.equals(BooleanType.get())) {
      return isNull ? null : value.getValue();
    }

    if(valueType.equals(TextType.get())) {
      return isNull ? null : value.getValue();
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

  public boolean contains(Value testValue) {
    return getValue().isSequence() //
        ? getValue().asSequence().contains(testValue) //
        : getValue().equals(testValue);
  }

  @Nullable
  @Override
  public String toString() {
    return getValue().toString();
  }

  @Override
  public boolean hasMember(String name) {
    return getMembers().containsKey(name);
  }

  @Override
  public Object getMember(final String name) {
    return getMembers().get(name);
  }

  @Override
  public Set<String> keySet() {
    return getMembers().keySet();
  }

  private static Map<String, JSObject> members = Maps.newConcurrentMap();

  static {
    addMethodProvider(members, BooleanMethods.class);
    addMethodProvider(members, DateTimeMethods.class);
    addMethodProvider(members, TextMethods.class);
    addMethodProvider(members, ScriptableValueMethods.class);
    addMethodProvider(members, ValueSequenceMethods.class);
    addMethodProvider(members, NumericMethods.class);
    addMethodProvider(members, CompareMethods.class);
    addMethodProvider(members, UnitMethods.class);
    addMethodProvider(members, GeoMethods.class);
  }

  public static Map<String, JSObject> getMembers() {
    return members;
  }
}
