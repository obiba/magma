package org.obiba.magma.js.methods;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.jscience.physics.unit.system.SI;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.unitsofmeasurement.unit.Unit;

@SuppressWarnings({ "UnusedParameters", "UnusedDeclaration" })
public class NumericMethods {

  private NumericMethods() {
  }

  private enum Ops {
    DIVIDE() {
      @Override
      public BigDecimal operate(BigDecimal lhs, BigDecimal rhs) {
        return lhs.divide(rhs, MathContext.DECIMAL128);
      }

      @Override
      public Unit<?> operate(Unit<?> lhs, Unit<?> rhs) {
        return lhs.divide(rhs);
      }
    },
    MINUS() {
      @Override
      public BigDecimal operate(BigDecimal lhs, BigDecimal rhs) {
        return lhs.subtract(rhs);
      }
    },
    MULTIPLY() {
      @Override
      public BigDecimal operate(BigDecimal lhs, BigDecimal rhs) {
        return lhs.multiply(rhs);
      }

      @Override
      public Unit<?> operate(Unit<?> lhs, Unit<?> rhs) {
        return lhs.multiply(rhs);
      }
    },
    PLUS() {
      @Override
      public BigDecimal operate(BigDecimal lhs, BigDecimal rhs) {
        return lhs.add(rhs);
      }
    };

    /**
     * Performs this operation on the provided values and returns the result
     *
     * @param lhs
     * @param rhs
     * @return
     * @throws ArithmeticException when the operation cannot be performed on the operands (division by zero)
     */
    public abstract BigDecimal operate(BigDecimal lhs, BigDecimal rhs) throws ArithmeticException;

    public Unit<?> operate(Unit<?> lhs, Unit<?> rhs) {
      return lhs;
    }
  }

  private enum Comps {
    GT() {
      @Override
      public boolean apply(int value) {
        return value > 0;
      }
    },
    GE() {
      @Override
      public boolean apply(int value) {
        return value >= 0;
      }
    },
    LT() {
      @Override
      public boolean apply(int value) {
        return value < 0;
      }
    },
    LE() {
      @Override
      public boolean apply(int value) {
        return value <= 0;
      }
    },
    EQ() {
      @Override
      public boolean apply(int value) {
        return value == 0;
      }
    };

    public abstract boolean apply(int value);
  }

  private enum Unary {
    ABS() {
      @Override
      public BigDecimal operate(BigDecimal value, Object... args) {
        return value.abs();
      }
    },
    POW() {
      @Override
      public BigDecimal operate(BigDecimal value, Object... args) {
        BigDecimal power = asBigDecimal(args[0]);
        try {
          int intPower = power.intValueExact();
          return value.pow(intPower);
        } catch(ArithmeticException e) {
          return BigDecimal.valueOf(Math.pow(value.doubleValue(), power.doubleValue()));
        }
      }

      @Override
      public Unit<?> operate(Unit<?> unit, Object... args) {
        BigDecimal power = asBigDecimal(args[0]);
        try {
          int intPower = power.intValueExact();
          return unit.pow(intPower);
        } catch(ArithmeticException e) {
          return SI.ONE;
        }
      }
    },
    ROOT() {
      @Override
      public BigDecimal operate(BigDecimal value, Object... args) {
        if(args[0] instanceof Integer) {
          int intRoot = (Integer) args[0];
          switch(intRoot) {
            case 2:
              return BigDecimal.valueOf(Math.sqrt(value.doubleValue()));
            case 3:
              return BigDecimal.valueOf(Math.cbrt(value.doubleValue()));
          }
        }
        BigDecimal root = asBigDecimal(args[0]);
        return BigDecimal.valueOf(Math.pow(value.doubleValue(), 1 / root.doubleValue()));
      }

      @Override
      public Unit<?> operate(Unit<?> unit, Object... args) {
        BigDecimal root = asBigDecimal(args[0]);
        try {
          int intRoot = root.intValueExact();
          return unit.root(intRoot);
        } catch(ArithmeticException e) {
          return SI.ONE;
        }
      }
    },
    LOG() {
      @Override
      public BigDecimal operate(BigDecimal value, Object... args) {
        double log = Math.log10(value.doubleValue());
        if(args.length > 0) {
          double base = asBigDecimal(args[0]).doubleValue();
          log = log / Math.log10(base);
        }
        return BigDecimal.valueOf(log);
      }
    },
    LN() {
      @Override
      public BigDecimal operate(BigDecimal value, Object... args) {
        return BigDecimal.valueOf(Math.log(value.doubleValue()));
      }
    };

    /**
     * Performs this operation on the provided value and returns the result
     *
     * @param value
     * @return
     * @throws ArithmeticException when the operation cannot be performed on the operands (division by zero)
     */
    public abstract BigDecimal operate(BigDecimal value, Object... args) throws ArithmeticException;

    public Unit<?> operate(Unit<?> unit, Object... args) {
      return unit;
    }
  }

  /**
   * Returns a new {@link ScriptableValue} containing the sum of the caller and the supplied parameter. If both operands
   * are of IntegerType then the returned type will also be IntegerType, otherwise the returned type is DecimalType.
   * <p/>
   * <pre>
   *   $('NumberVarOne').plus($('NumberVarTwo'))
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue plus(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return operate((ScriptableValue) thisObj, args, Ops.PLUS);
  }

  /**
   * Returns a new {@link ScriptableValue} containing the result of the supplied parameter subtracted from the caller.
   * If both operands are of IntegerType then the returned type will also be IntegerType, otherwise the returned type is
   * DecimalType.
   * <p/>
   * <pre>
   *   $('NumberVarOne').minus($('NumberVarTwo'))
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue minus(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return operate((ScriptableValue) thisObj, args, Ops.MINUS);
  }

  /**
   * Returns a new {@link ScriptableValue} containing the result of the supplied parameter multiplied by the caller. If
   * both operands are of IntegerType then the returned type will also be IntegerType, otherwise the returned type is
   * DecimalType.
   * <p/>
   * <pre>
   *   $('NumberVarOne').multiply($('NumberVarTwo'))
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue multiply(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return operate((ScriptableValue) thisObj, args, Ops.MULTIPLY);
  }

  /**
   * Returns a new {@link ScriptableValue} containing the result of the caller divided by the supplied parameter. The
   * return type is always DecimalType.
   * <p/>
   * <pre>
   *   $('NumberVarOne').div($('NumberVarTwo'))
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue div(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return operate((ScriptableValue) thisObj, args, Ops.DIVIDE);
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link BooleanType} indicating if the first parameter is greater than
   * the second parameter.
   * <p/>
   * <pre>
   *   $('NumberVarOne').gt($('NumberVarTwo'))
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue gt(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, compare((ScriptableValue) thisObj, args, Comps.GT));
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link BooleanType} indicating if the first parameter is greater than
   * or equal the second parameter.
   * <p/>
   * <pre>
   *   $('NumberVarOne').ge($('NumberVarTwo'))
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue ge(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, compare((ScriptableValue) thisObj, args, Comps.GE));
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link BooleanType} indicating if the first parameter is less than the
   * second parameter.
   * <p/>
   * <pre>
   *   $('NumberVarOne').lt($('NumberVarTwo'))
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue lt(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, compare((ScriptableValue) thisObj, args, Comps.LT));
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link BooleanType} indicating if the first parameter is less than or
   * equal the second parameter.
   * <p/>
   * <pre>
   *   $('NumberVarOne').le($('NumberVarTwo'))
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue le(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, compare((ScriptableValue) thisObj, args, Comps.LE));
  }

  /**
   * Returns the absolute value of the input value.
   * <p/>
   * <pre>
   *   $('NumberVarOne').abs()
   * </pre>
   */
  public static ScriptableValue abs(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return operate((ScriptableValue) thisObj, args, Unary.ABS);
  }

  /**
   * Returns a new {@link ScriptableValue} that is the natural logarithm of this value.
   * <p/>
   * <pre>
   *   $('NumberVarOne').ln()
   * </pre>
   */
  public static ScriptableValue ln(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return operate((ScriptableValue) thisObj, args, Unary.LN);
  }

  /**
   * Returns a new {@link ScriptableValue} that is the natural logarithm of this value.
   * <p/>
   * <pre>
   *   $('NumberVarOne').log() // log base 10
   *   $('NumberVarOne').log(2) // log base 2
   * </pre>
   */
  public static ScriptableValue log(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return operate((ScriptableValue) thisObj, args, Unary.LOG);
  }

  /**
   * Returns a new {@link ScriptableValue} that is the value raised to the specified power.
   * <p/>
   * <pre>
   *   $('NumberVarOne').pow(2)
   *   $('NumberVarOne').pow(-2)
   * </pre>
   */
  public static ScriptableValue pow(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return operate((ScriptableValue) thisObj, args, Unary.POW);
  }

  /**
   * Returns a new {@link ScriptableValue} that is the value's {@code root} root.
   * <p/>
   * <pre>
   *   $('NumberVarOne').sqroot() // square root
   *   $('NumberVarOne').cbroot() // cubic root
   *   $('NumberVarOne').root(42) // arbitrary root
   * </pre>
   */
  public static ScriptableValue root(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return operate((ScriptableValue) thisObj, args, Unary.ROOT);
  }

  public static ScriptableValue sqroot(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return operate((ScriptableValue) thisObj, new Object[] { 2 }, Unary.ROOT);
  }

  public static ScriptableValue cbroot(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return operate((ScriptableValue) thisObj, new Object[] { 3 }, Unary.ROOT);
  }

  /**
   * Groups variables in continuous space into discrete space given a list of adjacent range limits. When the current
   * value is not an integer a null value is returned.
   * <p/>
   * <pre>
   * // usage example, possible returned values are: '-18', '18-35', '35-40', ..., '70+'
   * $('CURRENT_AGE').group([18,35,40,45,50,55,60,65,70]);
   *
   * // support of optional outliers
   * $('CURRENT_AGE').group([18,35,40,45,50,55,60,65,70],[888,999]);
   *
   * // in combination with map
   * $('CURRENT_AGE').group([30,40,50,60],[888,999]).map({
   *    '-30' :  1,
   *    '30-40': 2,
   *    '40-50': 3,
   *    '50-60': 4,
   *    '60+':   5,
   *    '888':   88,
   *    '999':   99
   *  });
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @return funObj
   */
  public static ScriptableValue group(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {

    if(args == null || args.length < 1 || !(args[0] instanceof NativeArray)) {
      throw new MagmaJsEvaluationRuntimeException("illegal arguments to group()");
    }

    if(args.length == 2 && !(args[1] instanceof NativeArray)) {
      throw new MagmaJsEvaluationRuntimeException("illegal arguments to group()");
    }

    ScriptableValue sv = (ScriptableValue) thisObj;
    Value currentValue = sv.getValue();

    List<Value> boundaries = boundaryValues(sv.getValueType(), args);
    List<Value> outliers = outlierValues(sv.getValueType(), args);

    if(currentValue.isSequence()) {
      if(currentValue.isNull()) {
        return new ScriptableValue(thisObj, TextType.get().nullSequence());
      }
      Collection<Value> newValues = new ArrayList<>();
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(lookupGroup(ctx, thisObj, value, boundaries, outliers));
      }
      return new ScriptableValue(thisObj, TextType.get().sequenceOf(newValues));
    }
    return new ScriptableValue(thisObj, lookupGroup(ctx, thisObj, currentValue, boundaries, outliers));
  }

  /**
   * Returns the boundary list value to be used when present. Otherwise use the corresponding range. This method is used
   * by the group() method.
   *
   * @param valueType
   * @param args
   * @return
   */
  private static List<Value> boundaryValues(ValueType valueType, Object... args) {
    return nativeArrayToValueList(valueType, args[0]);
  }

  /**
   * Returns the outlier list value to be used when present. Otherwise use the corresponding range. This method is used
   * by the group() method.
   *
   * @param valueType
   * @param args
   * @return
   */
  private static List<Value> outlierValues(ValueType valueType, Object... args) {
    return args.length > 1 ? nativeArrayToValueList(valueType, args[1]) : null;
  }

  /**
   * Returns a List of Value from a NativeArray. This method is used by boundaryValues() and outlierValues(). by the
   * group() method.
   *
   * @param valueType
   * @param args
   * @return
   */
  private static List<Value> nativeArrayToValueList(ValueType valueType, Object array) {
    NativeArray a = (NativeArray) array;
    List<Value> newValues = new ArrayList<>();
    Value newValue;
    for(int index = 0; index < (int) a.getLength(); index++) {
      newValue = valueType.valueOf(a.get(index, a));
      newValues.add(index, newValue);
    }
    Collections.sort(newValues);
    return newValues;
  }

  /**
   * Lookup {@code value} within {@code boundaries} and return the corresponding group of type integer
   *
   * @param ctx
   * @param thisObj
   * @param value
   * @param boundaries
   * @param outliers
   * @return
   */
  private static Value lookupGroup(Context ctx, Scriptable thisObj, Value value, Iterable<Value> boundaries,
      Collection<Value> outliers) {
    if(outliers != null && outliers.contains(value)) {
      return TextType.get().convert(value);
    }
    if(value.isNull()) {
      return TextType.get().nullValue();
    }

    Value lowerBound = null;
    // boundaries are ordered
    for(Value upperBound : boundaries) {
      if(value.compareTo(upperBound) < 0) {
        return lowerBound == null //
            ? TextType.get().valueOf("-" + formatNumberValue(upperBound)) //
            : TextType.get().valueOf(formatNumberValue(lowerBound) + "-" + formatNumberValue(upperBound));
      }
      lowerBound = upperBound;
    }
    if(lowerBound != null && value.compareTo(lowerBound) >= 0) {
      return TextType.get().valueOf(formatNumberValue(lowerBound) + "+");
    }

    // no boundaries
    return TextType.get().valueOf(formatNumberValue(value));
  }

  /**
   * Remove the trailing '.0' that appear for doubles in java, but not in javascript.
   *
   * @param value
   * @return
   */
  private static String formatNumberValue(Value value) {
    if(value.isNull()) return null;
    String str = value.toString();
    if(str == null) return null;
    return str.endsWith(".0") ? str.substring(0, str.length() - 2) : str;
  }

  static Value equals(ScriptableValue thisObj, Object... args) {
    return compare(thisObj, args, Comps.EQ);
  }

  static Value compare(ScriptableValue thisObj, Object args[], Comps comparator) {
    BigDecimal value = asBigDecimal(thisObj);
    if(value == null) return BooleanType.get().nullValue();
    for(Object argument : args) {
      BigDecimal rhs = asBigDecimal(argument);
      if(rhs == null) return BooleanType.get().nullValue();
      if(!comparator.apply(value.compareTo(rhs))) {
        return BooleanType.get().falseValue();
      }
    }
    return BooleanType.get().trueValue();
  }

  static ScriptableValue operate(ScriptableValue thisObj, Object args[], Unary operation) {
    try {
      BigDecimal value = asBigDecimal(thisObj);
      if(value == null) return new ScriptableValue(thisObj, thisObj.getValueType().nullValue());
      value = operation.operate(value, args);
      Unit<?> unit = operation.operate(UnitMethods.extractUnit(thisObj), args);
      try {
        long longValue = value.longValueExact();
        return new ScriptableValue(thisObj, IntegerType.get().valueOf(longValue), unit.toString());
      } catch(ArithmeticException e) {
        return new ScriptableValue(thisObj, DecimalType.get().valueOf(value.doubleValue()), unit.toString());
      }
    } catch(ArithmeticException e) {
      return new ScriptableValue(thisObj, DecimalType.get().nullValue());
    }
  }

  static ScriptableValue operate(ScriptableValue thisObj, Object args[], Ops operation) {
    try {
      BigDecimal value = asBigDecimal(thisObj);
      if(value == null) return new ScriptableValue(thisObj, thisObj.getValueType().nullValue());
      Unit<?> unit = UnitMethods.extractUnit(thisObj);
      for(Object argument : args) {
        BigDecimal rhs = asBigDecimal(argument);
        if(rhs == null) return new ScriptableValue(thisObj, thisObj.getValueType().nullValue());
        value = operation.operate(value, rhs);
        unit = operation.operate(unit, UnitMethods.extractUnit(argument));
      }

      try {
        long longValue = value.longValueExact();
        return new ScriptableValue(thisObj, IntegerType.get().valueOf(longValue), unit.toString());
      } catch(ArithmeticException e) {
        return new ScriptableValue(thisObj, DecimalType.get().valueOf(value.doubleValue()), unit.toString());
      }
    } catch(ArithmeticException e) {
      return new ScriptableValue(thisObj, DecimalType.get().nullValue());
    }
  }

  static Double asDouble(Object obj) {
    if(obj == null) return null;
    if(obj instanceof Number) {
      return ((Number) obj).doubleValue();
    }
    if(obj instanceof ScriptableValue) {
      ScriptableValue sv = (ScriptableValue) obj;
      Value value = sv.getValue();
      return value.isNull() ? null : ((Number) value.getValue()).doubleValue();
    }
    if(obj instanceof String) {
      return Double.valueOf((String) obj);
    }
    throw new IllegalArgumentException("cannot interpret argument as number: '" + obj + "'");
  }

  static BigDecimal asBigDecimal(Object object) {
    if(object == null) return null;

    if(object instanceof ScriptableValue) {
      return asBigDecimal((ScriptableValue) object);
    }
    if(object instanceof Number) {
      return new BigDecimal(object.toString());
    }
    if(object instanceof String) {
      return new BigDecimal((String) object);
    }
    throw new IllegalArgumentException("cannot interpret argument as number: '" + object + "'");
  }

  static BigDecimal asBigDecimal(ScriptableValue scriptableValue) {
    if(scriptableValue == null) throw new IllegalArgumentException("value cannot be null");
    Value value = scriptableValue.getValue();
    if(value.isNull()) {
      // Throw a runtime exception if the null value provided in scriptableValue argument is not convertible to decimal.
      // This is to manipulate the null value only created by a "Number" Type.
      ValueType.Factory.converterFor(scriptableValue.getValueType(), DecimalType.get());
      return null;
    }
    if(scriptableValue.getValueType().isNumeric()) {
      return new BigDecimal(((Number) value.getValue()).doubleValue());
    }
    Value decimalValue = DecimalType.get().convert(value);
    return new BigDecimal((Double) decimalValue.getValue());
  }

  static Double sum(ValueSequence valueSequence) {
    if(valueSequence.isNull()) return null;

    double sum = 0;
    for(Value v : valueSequence.getValue()) {
      if(v.isNull()) {
        return null;
      }
      sum += ((Number) v.getValue()).doubleValue();
    }
    return sum;
  }

  static Double average(ValueSequence valueSequence) {
    int size = valueSequence.getSize();
    if(size == 0) return null;
    Double sum = sum(valueSequence);
    if(sum != null) {
      return sum / size;
    }
    return null;
  }

  static Double stddev(ValueSequence valueSequence) {
    if(valueSequence.isNull()) return null;

    Double avg = average(valueSequence);
    if(avg == null) return null;

    int size = valueSequence.getSize();
    if(size == 0) return null;

    double sumDev = 0;
    for(Value v : valueSequence.getValue()) {
      if(v.isNull()) {
        return null;
      }
      double d = ((Number) v.getValue()).doubleValue();
      sumDev += (d - avg) * (d - avg);
    }

    return Math.sqrt(sumDev / size);
  }

}
