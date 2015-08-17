package org.obiba.magma.js.methods;

import java.text.ParsePosition;
import java.util.ResourceBundle;

import javax.annotation.Nullable;

import org.jscience.physics.unit.PhysicsUnit;
import org.jscience.physics.unit.format.SymbolMap;
import org.jscience.physics.unit.format.UCUMFormat;
import org.jscience.physics.unit.system.SI;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.TextType;
import org.unitsofmeasurement.unit.Unit;

import com.google.common.base.Strings;

public class UnitMethods {

  /**
   * UCUM does not have a default for overloaded units (international inches vs. us inches vs. british inches). This
   * UCUMFormat instance maps some common symbols to the international standard or the US standard when no international
   * standard exists. Mass units use the 'avoirdupoids' definitions.
   * <p/>
   * Only three units overlap with SI notation: ft, pt (pint) and yd. This results in masking the "femto-ton",
   * "pico-ton" and the "yotta-day", so it should not have any impact as these are not common units and can be expressed
   * differently.
   */
  private static final UCUMFormat DEFAULTS = UCUMFormat
      .getCaseSensitiveInstance(new SymbolMap(ResourceBundle.getBundle(UnitMethods.class.getName() + "_CS")));

  private UnitMethods() {}

  /**
   * Forces the unit to a specific value (one argument) or returns the current unit value if no arguments are provided.
   * <p/>
   * <pre>
   * $('HEIGHT').unit('cm')
   * $('HEIGHT').unit('cm').unit().any('cm') // returns true
   * </pre>
   */
  public static ScriptableValue unit(ScriptableValue thisObj, Object[] args)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue value = thisObj;

    if(args.length == 1) {
      String newUnit = asString(args[0]);
      return new ScriptableValue(value.getValue(), newUnit);
    }
    return new ScriptableValue(TextType.get().valueOf(value.getUnit()));
  }

  /**
   * Converts the current value to the specified unit. The current value must have a non-empty unit for this method to
   * succeed.
   * <p/>
   * <pre>
   * $('HEIGHT').toUnit('cm')
   * </pre>
   */
  @SuppressWarnings("unchecked")
  public static ScriptableValue toUnit(ScriptableValue thisObj, Object[] args)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue value = thisObj;

    @SuppressWarnings("rawtypes")
    Unit target = extractUnit(args[0]);

    if(value.getValue().isNull()) {
      return new ScriptableValue(value.getValue(), target.toString());
    }

    Unit<?> source = extractUnit(value);

    if(target == SI.ONE) {
      throw new MagmaJsEvaluationRuntimeException(String.format("unknown target unit %s", args[0]));
    }

    if(source == SI.ONE) {
      if(Strings.isNullOrEmpty(value.getUnit())) {
        throw new MagmaJsEvaluationRuntimeException(
            String.format("current unit is not specified. use unit() method to specify it."));
      }
      throw new MagmaJsEvaluationRuntimeException(String.format("current unit is unknown: '%s'.", value.getUnit()));
    }

    if(!target.isCompatible(source)) {
      throw new MagmaJsEvaluationRuntimeException(String.format("unit %s cannot be converted to %s", source, target));
    }

    double sourceValue = (Double) DecimalType.get().convert(value.getValue()).getValue();

    double newValue = source.getConverterTo(target).convert(sourceValue);

    return new ScriptableValue(DecimalType.get().valueOf(newValue), target.toString());
  }

  @SuppressWarnings("ChainOfInstanceofChecks")
  public static Unit<?> extractUnit(Object value) {
    if(value == null) return SI.ONE;
    if(value instanceof ScriptableValue) return extractUnit((ScriptableValue) value);
    if(value instanceof String) return extractUnit((String) value);
    return SI.ONE;
  }

  public static Unit<?> extractUnit(String value) {
    if(value == null) return SI.ONE;
    try {
      // Try the common non-UCUM notation strings
      // This is tried first to use commonly used units like ft and yd instead of uncommon femto-ton (ft), etc.
      return DEFAULTS.parse(value, new ParsePosition(0));
    } catch(IllegalArgumentException e) {
      try {
        return PhysicsUnit.valueOf(value);
      } catch(IllegalArgumentException ignored) {
      }
    }
    return SI.ONE;
  }

  public static Unit<?> extractUnit(ScriptableValue scriptableValue) {
    if(scriptableValue.hasUnit()) {
      return extractUnit(scriptableValue.getUnit());
    }
    return SI.ONE;
  }

  @Nullable
  private static String asString(Object arg) {
    if(arg == null) return null;

    if(arg instanceof String) return (String) arg;

    if(arg instanceof ScriptableValue) {
      ScriptableValue value = (ScriptableValue) arg;
      return value.getValue().toString();
    }

    return arg.toString();
  }
}
