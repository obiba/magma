package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

public class CompareMethods {

  /**
   * Returns a new {@link ScriptableValue} of the {@link IntegerType} indicating if the first parameter is greater than,
   * equal to, or less than the second parameter. Both parameters must either both be numeric, both be BooleanType or
   * both be TextType. Zero (0) is returned if the values are equal. A number greater than zero (1) is returned if the
   * first paramter is greater than the second. A number less than zero (-1) is returned is the first parameter is less
   * than the second.
   * 
   * <pre>
   *   $('NumberVarOne').compare($('NumberVarTwo'))
   *   $('BooleanVarOne').compare($('BooleanVarTwo'))
   *   $('TextVarOne').compare($('TextVarTwo'))
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of a numeric type,
   * BooleanType or TextType. Also thrown if operands are null.
   */
  public static ScriptableValue compare(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue firstOperand = (ScriptableValue) thisObj;
    if(firstOperand.getValue().isNull()) throw new MagmaJsEvaluationRuntimeException("Cannot invoke compare() with null argument.");
    if(args != null && args.length > 0 && args[0] instanceof ScriptableValue && !((ScriptableValue) args[0]).getValue().isNull()) {
      ScriptableValue secondOperand = (ScriptableValue) args[0];
      if(firstOperand.getValueType().isNumeric() && secondOperand.getValueType().isNumeric()) {
        return numericCompare(thisObj, firstOperand, secondOperand);
      } else if(firstOperand.getValueType().equals(BooleanType.get()) && secondOperand.getValueType().equals(BooleanType.get())) {
        return booleanCompare(thisObj, firstOperand, secondOperand);
      } else if(firstOperand.getValueType().equals(TextType.get()) && secondOperand.getValueType().equals(TextType.get())) {
        return textCompare(thisObj, firstOperand, secondOperand);
      } else {
        throw new MagmaJsEvaluationRuntimeException("Cannot invoke compare() with arguments of type '" + firstOperand.getValueType().getName() + "' and '" + secondOperand.getValueType().getName() + "'.");
      }
    } else {
      throw new MagmaJsEvaluationRuntimeException("Cannot invoke compare() with null argument or argument that is not a ScriptableValue.");
    }
  }

  private static ScriptableValue numericCompare(Scriptable thisObj, ScriptableValue firstOperand, ScriptableValue secondOperand) {
    Number firstNumber = (Number) firstOperand.getValue().getValue();
    Number secondNumber = (Number) secondOperand.getValue().getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(((Long) firstNumber).compareTo(((Long) secondNumber))));
    } else if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(Double.compare(((Long) firstNumber).doubleValue(), (Double) secondNumber)));
    } else if(firstOperand.getValueType().equals(DecimalType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(Double.compare((Double) firstNumber, ((Long) secondNumber).doubleValue())));
    } else {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(((Double) firstNumber).compareTo(((Double) secondNumber))));
    }
  }

  private static ScriptableValue booleanCompare(Scriptable thisObj, ScriptableValue firstOperand, ScriptableValue secondOperand) {
    Boolean firstBoolean = (Boolean) firstOperand.getValue().getValue();
    Boolean secondBoolean = (Boolean) secondOperand.getValue().getValue();
    return new ScriptableValue(thisObj, IntegerType.get().valueOf(firstBoolean.compareTo(secondBoolean)));
  }

  private static ScriptableValue textCompare(Scriptable thisObj, ScriptableValue firstOperand, ScriptableValue secondOperand) {
    String firstString = (String) firstOperand.getValue().getValue();
    String secondString = (String) secondOperand.getValue().getValue();
    return new ScriptableValue(thisObj, IntegerType.get().valueOf(firstString.compareTo(secondString)));
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link IntegerType} indicating if the first parameter is greater than,
   * equal to, or less than the second parameter. Both parameters must be TextType. Case is ignored. Zero (0) is
   * returned if the values are equal. A number greater than zero (1) is returned if the first paramter is greater than
   * the second. A number less than zero (-1) is returned is the first parameter is less than the second.
   * 
   * <pre>
   *   $('TextVarOne').compareNoCase($('TextVarTwo'))
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of TextType. Also thrown if
   * operands are null.
   */
  public static ScriptableValue compareNoCase(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue firstOperand = (ScriptableValue) thisObj;
    if(firstOperand.getValue().isNull()) throw new MagmaJsEvaluationRuntimeException("Cannot invoke compareNoCase() with null argument.");
    if(args != null && args.length > 0 && args[0] instanceof ScriptableValue && !((ScriptableValue) args[0]).getValue().isNull()) {
      ScriptableValue secondOperand = (ScriptableValue) args[0];
      if(firstOperand.getValueType().equals(TextType.get()) && secondOperand.getValueType().equals(TextType.get())) {
        String firstString = (String) firstOperand.getValue().getValue();
        String secondString = (String) secondOperand.getValue().getValue();
        return new ScriptableValue(thisObj, IntegerType.get().valueOf(firstString.compareToIgnoreCase(secondString)));
      } else {
        throw new MagmaJsEvaluationRuntimeException("Cannot invoke compareNoCase() with arguments of type '" + firstOperand.getValueType().getName() + "' and '" + secondOperand.getValueType().getName() + "'. Use type 'text' only.");
      }
    } else {
      throw new MagmaJsEvaluationRuntimeException("Cannot invoke compareNoCase() with null argument or argument that is not a ScriptableValue.");
    }
  }
}
