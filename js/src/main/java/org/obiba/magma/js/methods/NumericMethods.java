package org.obiba.magma.js.methods;

import java.math.BigDecimal;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;

public class NumericMethods {

  private enum Ops {
    DIVIDE() {

      @Override
      public boolean alwaysDecimal() {
        return true;
      }

      @Override
      public BigDecimal operate(BigDecimal lhs, BigDecimal rhs) {
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
    },
    PLUS() {

      @Override
      public BigDecimal operate(BigDecimal lhs, BigDecimal rhs) {
        return lhs.add(rhs);
      }
    };

    /**
     * Returns true when the operation always produces a decimal (default is false).
     * @return
     */
    public boolean alwaysDecimal() {
      return false;
    }

    /**
     * Performs this operation on the provided values and returns the result
     * 
     * @param lhs
     * @param rhs
     * @return
     * @throws ArithmeticException when the operation cannot be performed on the operands (division by zero)
     */
    public abstract BigDecimal operate(BigDecimal lhs, BigDecimal rhs) throws ArithmeticException;
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
    };

    public abstract boolean apply(int value);
  }

  /**
   * Returns a new {@link ScriptableValue} containing the sum of the caller and the supplied parameter. If both operands
   * are of IntegerType then the returned type will also be IntegerType, otherwise the returned type is DecimalType.
   * 
   * <pre>
   *   $('NumberVarOne').plus($('NumberVarTwo'))
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue plus(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, operate((ScriptableValue) thisObj, args, Ops.PLUS));
  }

  /**
   * Returns a new {@link ScriptableValue} containing the result of the supplied parameter subtracted from the caller.
   * If both operands are of IntegerType then the returned type will also be IntegerType, otherwise the returned type is
   * DecimalType.
   * 
   * <pre>
   *   $('NumberVarOne').minus($('NumberVarTwo'))
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue minus(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, operate((ScriptableValue) thisObj, args, Ops.MINUS));
  }

  /**
   * Returns a new {@link ScriptableValue} containing the result of the supplied parameter multiplied by the caller. If
   * both operands are of IntegerType then the returned type will also be IntegerType, otherwise the returned type is
   * DecimalType.
   * 
   * <pre>
   *   $('NumberVarOne').multiply($('NumberVarTwo'))
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue multiply(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, operate((ScriptableValue) thisObj, args, Ops.MULTIPLY));
  }

  /**
   * Returns a new {@link ScriptableValue} containing the result of the caller divided by the supplied parameter. The
   * return type is always DecimalType.
   * 
   * <pre>
   *   $('NumberVarOne').div($('NumberVarTwo'))
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue div(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, operate((ScriptableValue) thisObj, args, Ops.DIVIDE));
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link BooleanType} indicating if the first parameter is greater than
   * the second parameter.
   * 
   * <pre>
   *   $('NumberVarOne').gt($('NumberVarTwo'))
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue gt(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, compare((ScriptableValue) thisObj, args, Comps.GT));
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link BooleanType} indicating if the first parameter is greater than
   * or equal the second parameter.
   * 
   * <pre>
   *   $('NumberVarOne').ge($('NumberVarTwo'))
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue ge(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, compare((ScriptableValue) thisObj, args, Comps.GE));
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link BooleanType} indicating if the first parameter is less than the
   * second parameter.
   * 
   * <pre>
   *   $('NumberVarOne').lt($('NumberVarTwo'))
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue lt(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, compare((ScriptableValue) thisObj, args, Comps.LT));
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link BooleanType} indicating if the first parameter is less than or
   * equal the second parameter.
   * 
   * <pre>
   *   $('NumberVarOne').le($('NumberVarTwo'))
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of IntegerType or
   * DecimalType.
   */
  public static ScriptableValue le(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    return new ScriptableValue(thisObj, compare((ScriptableValue) thisObj, args, Comps.LE));
  }

  static Value compare(ScriptableValue thisObj, Object args[], Comps comparator) {
    BigDecimal value = asBigDecimal(thisObj);
    for(Object argument : args) {
      BigDecimal rhs = asBigDecimal(argument);
      if(comparator.apply(value.compareTo(rhs)) == false) {
        return BooleanType.get().falseValue();
      }
    }
    return BooleanType.get().trueValue();
  }

  static Value operate(ScriptableValue thisObj, Object args[], Ops operation) {
    try {
      boolean allIntegerTypes = operation.alwaysDecimal() == false && thisObj.getValueType() == IntegerType.get();

      BigDecimal value = asBigDecimal(thisObj);
      for(Object argument : args) {
        allIntegerTypes &= isIntegerType(argument);
        BigDecimal rhs = asBigDecimal(argument);
        value = operation.operate(value, rhs);
      }
      if(allIntegerTypes) {
        return IntegerType.get().valueOf(value.longValue());
      }
      return DecimalType.get().valueOf(value.doubleValue());
    } catch(ArithmeticException e) {
      return DecimalType.get().nullValue();
    }
  }

  static boolean isIntegerType(Object object) {
    if(object == null) {
      // A null object can be represented as Zero
      return true;
    }
    if(object instanceof ScriptableValue) {
      return ((ScriptableValue) object).getValueType() == IntegerType.get();
    }
    return ValueType.Factory.forClass(object.getClass()) == IntegerType.get();
  }

  static BigDecimal asBigDecimal(Object object) {
    if(object == null) return BigDecimal.ZERO;

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
    if(scriptableValue.getValue().isNull()) {
      return BigDecimal.ZERO;
    }
    if(scriptableValue.getValueType().isNumeric()) {
      return new BigDecimal(((Number) scriptableValue.getValue().getValue()).doubleValue());
    }
    Value value = DecimalType.get().convert(scriptableValue.getValue());
    return new BigDecimal((Double) value.getValue());
  }
}
