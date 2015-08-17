package org.obiba.magma.js.methods;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import org.obiba.magma.Coordinate;
import org.obiba.magma.Value;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.PointType;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public class GeoMethods {

  private GeoMethods() {
  }

  /**
   * Returns the longitude of a {@link PointType}.
   * <pre>
   *   $('Point').longitude()
   * </pre>
   */
  public static ScriptableValue longitude(ScriptableValue thisObj, @Nullable Object[] args) {
    ScriptableValue sv = thisObj;
    Value value = sv.getValue();
    if(value.isNull()) {
      return new ScriptableValue(DecimalType.get().nullSequence());
    }
    if(value.isSequence()) {
      Collection<Value> values = new ArrayList<>();
      for(Value val : value.asSequence().getValue()) {
        Double longitude = ((Coordinate) val.getValue()).getLongitude();
        values.add(DecimalType.get().valueOf(longitude));
      }
      return new ScriptableValue(DecimalType.get().sequenceOf(values));
    }
    Value newValue = PointType.get().valueOf(value.toString());
    Double longitude = ((Coordinate) newValue.getValue()).getLongitude();
    return new ScriptableValue(DecimalType.get().valueOf(longitude));
  }

  /**
   * Returns the latitude of a {@link PointType}.
   * <pre>
   *   $('Point').latitude()
   * </pre>
   */
  public static ScriptableValue latitude(ScriptableValue thisObj, @Nullable Object[] args) {
    ScriptableValue sv = thisObj;
    Value value = sv.getValue();
    if(value.isNull()) {
      return new ScriptableValue(DecimalType.get().nullSequence());
    }
    if(value.isSequence()) {
      Collection<Value> values = new ArrayList<>();
      for(Value val : value.asSequence().getValue()) {
        Double latitude = ((Coordinate) val.getValue()).getLatitude();
        values.add(DecimalType.get().valueOf(latitude));
      }
      return new ScriptableValue(DecimalType.get().sequenceOf(values));
    }
    Value newValue = PointType.get().valueOf(value.toString());
    Double latitude = ((Coordinate) newValue.getValue()).getLatitude();
    return new ScriptableValue(DecimalType.get().valueOf(latitude));
  }
}
