package org.obiba.magma.js.methods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Coordinate;
import org.obiba.magma.Value;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;

@SuppressWarnings("ConstantConditions")
public class GeoMethods {

  private GeoMethods() {

  }

  /**
   * Returns the longitude of a {@link PointType}.
   * <pre>
   *   $('Point').longitude()
   * </pre>
   */
  public static ScriptableValue longitude(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {

    ScriptableValue sv = (ScriptableValue) thisObj;
    Value value = sv.getValue();

    if(value.isSequence()) {
      Collection<Value> values = new ArrayList<Value>();
      for(Value val : value.asSequence().getValue()) {
        Double longitude = ((Coordinate) val.getValue()).getLongitude();
        values.add(DecimalType.get().valueOf(longitude));
      }
      return new ScriptableValue(thisObj, DecimalType.get().sequenceOf(values));
    } else {
      Value newValue = PointType.get().valueOf(value.toString());
      Double longitude = ((Coordinate) newValue.getValue()).getLongitude();
      return new ScriptableValue(thisObj, DecimalType.get().valueOf(longitude));
    }
  }

  /**
   * Returns the latitude of a {@link PointType}.
   * <pre>
   *   $('Point').latitude()
   * </pre>
   */
  public static ScriptableValue latitude(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {

    ScriptableValue sv = (ScriptableValue) thisObj;
    Value value = sv.getValue();
    if(value.isSequence()) {
      Collection<Value> values = new ArrayList<Value>();
      for(Value val : value.asSequence().getValue()) {
        Double latitude = ((Coordinate) val.getValue()).getLatitude();
        values.add(DecimalType.get().valueOf(latitude));
      }
      return new ScriptableValue(thisObj, DecimalType.get().sequenceOf(values));
    } else {
      Value newValue = PointType.get().valueOf(value.toString());
      Double latitude = ((Coordinate) newValue.getValue()).getLatitude();
      return new ScriptableValue(thisObj, DecimalType.get().valueOf(latitude));
    }
  }

  /**
   * Returns the size of a {@link PolygonType}.
   * <pre>
   *   $('Polygon').size()
   * </pre>
   */
  @SuppressWarnings({ "unchecked", "OverlyStrongTypeCast" })
  public static ScriptableValue size(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    Value value = sv.getValue();

    if(value.isSequence()) {
      Collection<Value> values = new ArrayList<Value>();
      for(Value val : value.asSequence().getValue()) {
        int polygonSize = ((List<List<Coordinate>>) val.getValue()).size();
        values.add(IntegerType.get().valueOf(polygonSize));
      }
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(values));
    } else {
      Value newValue = PolygonType.get().valueOf(value.toString());
      int polygonSize = ((List<List<Coordinate>>) newValue.getValue()).size();
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(polygonSize));
    }
  }

}
