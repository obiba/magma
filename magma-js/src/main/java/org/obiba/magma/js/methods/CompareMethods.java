package org.obiba.magma.js.methods;

import javax.annotation.Nullable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

@SuppressWarnings(
    { "UnusedParameters", "StaticMethodOnlyUsedInOneClass" })
public class CompareMethods {

  private CompareMethods() {
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link IntegerType} indicating if the first parameter is greater than,
   * equal to, or less than the second parameter. Both parameters must either both be numeric, both be BooleanType or
   * both be TextType. Zero (0) is returned if the values are equal. A number greater than zero (1) is returned if the
   * first parameter is greater than the second. A number less than zero (-1) is returned is the first parameter is
   * less
   * than the second.
   * <p/>
   * <pre>
   *   $('NumberVarOne').compare($('NumberVarTwo'))
   *   $('BooleanVarOne').compare($('BooleanVarTwo'))
   *   $('TextVarOne').compare($('TextVarTwo'))
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of a numeric type,
   * BooleanType or TextType. Also thrown if operands are null.
   */
  public static ScriptableValue compare(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue firstOperand = (ScriptableValue) thisObj;
    if(firstOperand.getValue().isNull()) {
      throw new MagmaJsEvaluationRuntimeException("Cannot invoke compare() with null argument.");
    }
    if(args != null && args.length > 0 && args[0] instanceof ScriptableValue &&
        !((ScriptableValue) args[0]).getValue().isNull()) {
      ScriptableValue secondOperand = (ScriptableValue) args[0];
      if(firstOperand.getValueType().isNumeric() && secondOperand.getValueType().isNumeric()) {
        return numericCompare(thisObj, firstOperand, secondOperand);
      }
      if(firstOperand.getValueType().equals(BooleanType.get()) &&
          secondOperand.getValueType().equals(BooleanType.get())) {
        return booleanCompare(thisObj, firstOperand, secondOperand);
      }
      if(firstOperand.getValueType().equals(TextType.get()) && secondOperand.getValueType().equals(TextType.get())) {
        return textCompare(thisObj, firstOperand, secondOperand);
      }
      throw new MagmaJsEvaluationRuntimeException(
          "Cannot invoke compare() with arguments of type '" + firstOperand.getValueType().getName() + "' and '" +
              secondOperand.getValueType().getName() + "'.");
    }
    throw new MagmaJsEvaluationRuntimeException(
        "Cannot invoke compare() with null argument or argument that is not a ScriptableValue.");
  }

  private static ScriptableValue numericCompare(Scriptable thisObj, ScriptableValue firstOperand,
      ScriptableValue secondOperand) {
    Value firstOperandValue = firstOperand.getValue();
    Value secondOperandValue = secondOperand.getValue();
    if(firstOperandValue.isNull() && secondOperandValue.isNull()) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(0));
    }
    if(firstOperandValue.isNull()) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(-1));
    }
    if(secondOperandValue.isNull()) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(1));
    }
    Number firstNumber = (Number) firstOperandValue.getValue();
    Number secondNumber = (Number) secondOperandValue.getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) &&
        secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj,
          IntegerType.get().valueOf(((Long) firstNumber).compareTo((Long) secondNumber)));
    }
    if(firstOperand.getValueType().equals(IntegerType.get()) &&
        secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj,
          IntegerType.get().valueOf(Double.compare(firstNumber.doubleValue(), (Double) secondNumber)));
    }
    if(firstOperand.getValueType().equals(DecimalType.get()) &&
        secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj,
          IntegerType.get().valueOf(Double.compare((Double) firstNumber, secondNumber.doubleValue())));
    }
    return new ScriptableValue(thisObj,
        IntegerType.get().valueOf(((Double) firstNumber).compareTo((Double) secondNumber)));
  }

  private static ScriptableValue booleanCompare(Scriptable thisObj, ScriptableValue firstOperand,
      ScriptableValue secondOperand) {
    Value firstOperandValue = firstOperand.getValue();
    Boolean firstBoolean = firstOperandValue.isNull() ? Boolean.FALSE : (Boolean) firstOperandValue.getValue();
    Value secondOperandValue = secondOperand.getValue();
    Boolean secondBoolean = secondOperandValue.isNull() ? Boolean.FALSE : (Boolean) secondOperandValue.getValue();
    return new ScriptableValue(thisObj, IntegerType.get().valueOf(firstBoolean.compareTo(secondBoolean)));
  }

  private static ScriptableValue textCompare(Scriptable thisObj, ScriptableValue firstOperand,
      ScriptableValue secondOperand) {
    Value firstOperandValue = firstOperand.getValue();
    Value secondOperandValue = secondOperand.getValue();
    if(firstOperandValue.isNull() && secondOperandValue.isNull()) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(0));
    }
    if(firstOperandValue.isNull()) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(-1));
    }
    if(secondOperandValue.isNull()) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(1));
    }
    String firstString = (String) firstOperandValue.getValue();
    String secondString = (String) secondOperandValue.getValue();
    return new ScriptableValue(thisObj, IntegerType.get().valueOf(firstString.compareTo(secondString)));
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link IntegerType} indicating if the first parameter is greater than,
   * equal to, or less than the second parameter. Both parameters must be TextType. Case is ignored. Zero (0) is
   * returned if the values are equal. A number greater than zero (1) is returned if the first parameter is greater
   * than
   * the second. A number less than zero (-1) is returned is the first parameter is less than the second.
   * <p/>
   * <pre>
   *   $('TextVarOne').compareNoCase($('TextVarTwo'))
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of TextType. Also thrown if
   * operands are null.
   */
  public static ScriptableValue compareNoCase(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue firstOperand = (ScriptableValue) thisObj;
    Value firstOperandValue = firstOperand.getValue();
    if(firstOperandValue.isNull()) {
      throw new MagmaJsEvaluationRuntimeException("Cannot invoke compareNoCase() with null argument.");
    }
    if(args != null && args.length > 0 && args[0] instanceof ScriptableValue &&
        !((ScriptableValue) args[0]).getValue().isNull()) {
      ScriptableValue secondOperand = (ScriptableValue) args[0];
      if(firstOperand.getValueType().equals(TextType.get()) && secondOperand.getValueType().equals(TextType.get())) {
        String firstString = (String) firstOperandValue.getValue();
        Value secondOperandValue = secondOperand.getValue();
        String secondString = secondOperandValue.isNull() ? null : (String) secondOperandValue.getValue();
        return new ScriptableValue(thisObj, IntegerType.get().valueOf(firstString.compareToIgnoreCase(secondString)));
      }
      throw new MagmaJsEvaluationRuntimeException(
          "Cannot invoke compareNoCase() with arguments of type '" + firstOperand.getValueType().getName() +
              "' and '" + secondOperand.getValueType().getName() + "'. Use type 'text' only.");
    }
    throw new MagmaJsEvaluationRuntimeException(
        "Cannot invoke compareNoCase() with null argument or argument that is not a ScriptableValue.");
  }
}
