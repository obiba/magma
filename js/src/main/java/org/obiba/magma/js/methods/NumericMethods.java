package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;

public class NumericMethods {

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
    ScriptableValue firstOperand = getScriptableAsNumericScriptableValue(thisObj);
    ScriptableValue secondOperand = getFirstArgumentAsNumericScriptableValue(args);
    Number firstNumber = (Number) firstOperand.getValue().getValue();
    Number secondNumber = (Number) secondOperand.getValue().getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf((Long) firstNumber + (Long) secondNumber));
    } else if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Long) firstNumber + (Double) secondNumber));
    } else if(firstOperand.getValueType().equals(DecimalType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Double) firstNumber + (Long) secondNumber));
    } else {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Double) firstNumber + (Double) secondNumber));
    }
  }

  /**
   * Convert a non null {@link Scriptable} into a {@link ScriptableValue}.
   * @return A {@code ScriptableValue} containing a numeric {@link ValueType}.
   * @throws MagmaJsEvaluationRuntimeException if scriptable is not numeric
   */
  static ScriptableValue getScriptableAsNumericScriptableValue(Scriptable scriptable) throws MagmaJsEvaluationRuntimeException {
    if(scriptable != null && ((ScriptableValue) scriptable).getValueType().isNumeric()) {
      if(((ScriptableValue) scriptable).getValue().isNull()) {
        return new ScriptableValue(scriptable, IntegerType.get().valueOf(0));
      }
      return (ScriptableValue) scriptable;
    } else {
      throw new MagmaJsEvaluationRuntimeException("First operand to plus() method must be a ScriptableValue containing a IntegerType or DecimalType.");
    }
  }

  /**
   * Return the first argument of an array as a {@link ScriptableValue}.
   * @return A {@code ScriptableValue} containing a numeric {@link ValueType}.
   * @throws MagmaJsEvaluationRuntimeException if the first argument is not numeric
   */
  static ScriptableValue getFirstArgumentAsNumericScriptableValue(Object[] args) throws MagmaJsEvaluationRuntimeException {
    if(args != null && args.length > 0 && args[0] instanceof ScriptableValue && ((ScriptableValue) args[0]).getValueType().isNumeric()) {
      if(((ScriptableValue) args[0]).getValue().isNull()) {
        return new ScriptableValue((ScriptableValue) args[0], IntegerType.get().valueOf(0));
      }
      return (ScriptableValue) args[0];
    } else {
      throw new MagmaJsEvaluationRuntimeException("Second operand to plus() method must be a non null ScriptableValue containing a IntegerType or DecimalType.");
    }
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
    ScriptableValue firstOperand = getScriptableAsNumericScriptableValue(thisObj);
    ScriptableValue secondOperand = getFirstArgumentAsNumericScriptableValue(args);
    Number firstNumber = (Number) firstOperand.getValue().getValue();
    Number secondNumber = (Number) secondOperand.getValue().getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf((Long) firstNumber - (Long) secondNumber));
    } else if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Long) firstNumber - (Double) secondNumber));
    } else if(firstOperand.getValueType().equals(DecimalType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Double) firstNumber - (Long) secondNumber));
    } else {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Double) firstNumber - (Double) secondNumber));
    }
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
    ScriptableValue firstOperand = getScriptableAsNumericScriptableValue(thisObj);
    ScriptableValue secondOperand = getFirstArgumentAsNumericScriptableValue(args);
    Number firstNumber = (Number) firstOperand.getValue().getValue();
    Number secondNumber = (Number) secondOperand.getValue().getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf((Long) firstNumber * (Long) secondNumber));
    } else if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Long) firstNumber * (Double) secondNumber));
    } else if(firstOperand.getValueType().equals(DecimalType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Double) firstNumber * (Long) secondNumber));
    } else {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Double) firstNumber * (Double) secondNumber));
    }
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
    ScriptableValue firstOperand = getScriptableAsNumericScriptableValue(thisObj);
    ScriptableValue secondOperand = getFirstArgumentAsNumericScriptableValue(args);
    Number firstNumber = (Number) firstOperand.getValue().getValue();
    Number secondNumber = (Number) secondOperand.getValue().getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Long) firstNumber / ((Long) secondNumber).doubleValue()));
    } else if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Long) firstNumber / (Double) secondNumber));
    } else if(firstOperand.getValueType().equals(DecimalType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Double) firstNumber / (Long) secondNumber));
    } else {
      return new ScriptableValue(thisObj, DecimalType.get().valueOf((Double) firstNumber / (Double) secondNumber));
    }
  }

}
