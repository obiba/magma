package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Iterables;

/**
 * Methods of the {@code ScriptableValue} javascript class that deal with {@code ScriptableValue} of {@code BooleanType}
 * . Note that other methods that use {@code BooleanType} may be defined elsewhere.
 */
public class BooleanMethods {

  /**
   * <pre>
   *   $('Categorical').any('CAT1', 'CAT2')
   * </pre>
   * @return true when the value is equal to any of the parameter, false otherwise. Note that this method will always
   * return false if the value is null.
   */
  public static ScriptableValue any(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return buildValue(thisObj, false);
    }

    for(Object test : args) {
      Value testValue = sv.getValueType().valueOf(test);
      if(sv.contains(testValue)) {
        return buildValue(thisObj, true);
      }
    }
    return buildValue(thisObj, false);
  }

  /**
   * <pre>
   *   $('Categorical').all('CAT1', 'CAT2')
   * </pre>
   * @return true when the value contains all specified parameters, false otherwise. Note that this method will always
   * return false if the value is null.
   */
  public static ScriptableValue all(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return buildValue(thisObj, false);
    }

    for(Object test : args) {
      Value testValue = null;
      if(test instanceof String) {
        testValue = sv.getValueType().valueOf(test);
      } else if(test instanceof ScriptableValue) {
        testValue = ((ScriptableValue) test).getValue();
      } else {
        throw new MagmaJsEvaluationRuntimeException("cannot invoke all() with argument of type " + test.getClass().getName());
      }

      if(!sv.contains(testValue)) {
        return buildValue(thisObj, false);
      }
    }
    return buildValue(thisObj, true);
  }

  /**
   * <pre>
   *   $('BooleanVar').not()
   *   $('Categorical').any('CAT1').not()
   *   $('Categorical').not('CAT1', 'CAT2')
   *   $('Categorical').not($('Other Categorical'))
   * </pre>
   */
  public static ScriptableValue not(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(args != null && args.length > 0) {
      // Is of form .not(value)
      for(Object test : args) {
        Value testValue = sv.getValueType().valueOf(test);
        if(sv.contains(testValue)) {
          return buildValue(thisObj, false);
        }
      }
      return buildValue(thisObj, true);
    } else {
      // Is of form .not()
      Value value = sv.getValue();
      if(value.getValueType() == BooleanType.get()) {
        if(value.isSequence()) {
          // Transform the sequence of Boolean values to a sequence of !values
          Value notSeq = BooleanType.get().sequenceOf(Iterables.transform(value.asSequence().getValue(), new com.google.common.base.Function<Value, Value>() {
            @Override
            public Value apply(Value from) {
              // Transform the input into its invert boolean value
              return BooleanType.get().not(from);
            }

          }));
          return new ScriptableValue(thisObj, notSeq);
        }
        return new ScriptableValue(thisObj, BooleanType.get().not(value));
      }
      throw new MagmaJsEvaluationRuntimeException("cannot invoke not() for Value of type " + value.getValueType().getName());
    }
  }

  /**
   * <pre>
   *   $('BooleanVar').and(someBooleanVar)
   *   $('BooleanVar').and(firstBooleanVar, secondBooleanVar)
   *   $('BooleanVar').and($('OtheBooleanVar'))
   *   $('BooleanVar').and($('OtheBooleanVar').not())
   *   $('BooleanVar').and(someBooleanVar, $('OtheBooleanVar'))
   * </pre>
   */
  public static ScriptableValue and(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    Value value = sv.getValue();
    if(!value.getValueType().equals(BooleanType.get())) {
      throw new MagmaJsEvaluationRuntimeException("cannot invoke and() for Value of type " + value.getValueType().getName());
    }
    Boolean booleanValue = toBoolean(value);

    if(args == null || args.length == 0) {
      return buildValue(thisObj, booleanValue);
    }

    for(Object arg : args) {
      if(arg instanceof ScriptableValue) {
        ScriptableValue operand = (ScriptableValue) arg;
        booleanValue = ternaryAnd(booleanValue, toBoolean(operand.getValue()));
      } else {
        booleanValue = ternaryAnd(booleanValue, ScriptRuntime.toBoolean(arg));
      }
      if(Boolean.FALSE.equals(booleanValue)) {
        return buildValue(thisObj, false);
      }
    }
    return buildValue(thisObj, booleanValue);
  }

  /**
   * <pre>
   *   $('BooleanVar').isNull()
   * </pre>
   */
  public static ScriptableValue isNull(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, BooleanType.get().trueValue());
    }
    return new ScriptableValue(thisObj, BooleanType.get().falseValue());
  }

  /**
   * Returns true {@code BooleanType} if the {@code ScriptableValue} .empty() is operating on is a sequence that
   * contains zero values. Otherwise false is returned.
   * 
   * <pre>
   *   $('Admin.Interview.exportLog.destination').empty()
   * </pre>
   */
  public static ScriptableValue empty(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, BooleanType.get().nullValue());
    }

    if(sv.getValue().isSequence() && sv.getValue().asSequence().getSize() == 0) return new ScriptableValue(thisObj, BooleanType.get().trueValue());

    return new ScriptableValue(thisObj, BooleanType.get().falseValue());
  }

  private static ScriptableValue buildValue(Scriptable scope, Boolean value) {
    if(value == null) {
      return new ScriptableValue(scope, BooleanType.get().nullValue());
    }
    return new ScriptableValue(scope, value ? BooleanType.get().trueValue() : BooleanType.get().falseValue());
  }

  private static Boolean ternaryAnd(Boolean op1, Boolean op2) {
    if(op1 != null && !op1) {
      return false;
    }
    if(op2 != null && !op2) {
      return false;
    }
    if(op1 == null || op2 == null) {
      return null;
    }
    return true;
  }

  private static Boolean toBoolean(Value value) {
    return !value.isNull() ? (Boolean) value.getValue() : null;
  }

  /**
   * <pre>
   *   $('BooleanVar').or(someBooleanVar)
   *   $('BooleanVar').or($('OtheBooleanVar'))
   *   $('BooleanVar').or($('OtheBooleanVar').not())
   * </pre>
   */
  public static ScriptableValue or(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    Value value = sv.getValue();
    if(!value.getValueType().equals(BooleanType.get())) {
      throw new MagmaJsEvaluationRuntimeException("cannot invoke and() for Value of type " + value.getValueType().getName());
    }
    Boolean booleanValue = toBoolean(value);

    if(args == null || args.length == 0) {
      return buildValue(thisObj, booleanValue);
    }
    if(args[0] instanceof ScriptableValue) {
      ScriptableValue operand = (ScriptableValue) args[0];
      booleanValue = ternaryOr(booleanValue, toBoolean(operand.getValue()));
    } else {
      booleanValue = ternaryOr(booleanValue, ScriptRuntime.toBoolean(args[0]));
    }
    return buildValue(thisObj, booleanValue);
  }

  private static Boolean ternaryOr(Boolean op1, Boolean op2) {
    if(op1 == null && op2 == null) return null;
    if(op1 == null && op2 != null && op2) return true;
    if(op1 == null && op2 != null && !op2) return null;
    if(op2 == null && op1 != null && !op1) return null;
    return op1 || op2;
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
    ScriptableValue firstOperand = NumericMethods.getScriptableAsNumericScriptableValue(thisObj);
    ScriptableValue secondOperand = NumericMethods.getFirstArgumentAsNumericScriptableValue(args);
    Number firstNumber = (Number) firstOperand.getValue().getValue();
    Number secondNumber = (Number) secondOperand.getValue().getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Long) firstNumber > (Long) secondNumber));
    } else if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Long) firstNumber > (Double) secondNumber));
    } else if(firstOperand.getValueType().equals(DecimalType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Double) firstNumber > (Long) secondNumber));
    } else {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Double) firstNumber > (Double) secondNumber));
    }
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
    ScriptableValue firstOperand = NumericMethods.getScriptableAsNumericScriptableValue(thisObj);
    ScriptableValue secondOperand = NumericMethods.getFirstArgumentAsNumericScriptableValue(args);
    Number firstNumber = (Number) firstOperand.getValue().getValue();
    Number secondNumber = (Number) secondOperand.getValue().getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Long) firstNumber >= (Long) secondNumber));
    } else if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Long) firstNumber >= (Double) secondNumber));
    } else if(firstOperand.getValueType().equals(DecimalType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Double) firstNumber >= (Long) secondNumber));
    } else {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Double) firstNumber >= (Double) secondNumber));
    }
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
    ScriptableValue firstOperand = NumericMethods.getScriptableAsNumericScriptableValue(thisObj);
    ScriptableValue secondOperand = NumericMethods.getFirstArgumentAsNumericScriptableValue(args);
    Number firstNumber = (Number) firstOperand.getValue().getValue();
    Number secondNumber = (Number) secondOperand.getValue().getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Long) firstNumber < (Long) secondNumber));
    } else if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Long) firstNumber < (Double) secondNumber));
    } else if(firstOperand.getValueType().equals(DecimalType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Double) firstNumber < (Long) secondNumber));
    } else {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Double) firstNumber < (Double) secondNumber));
    }
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
    ScriptableValue firstOperand = NumericMethods.getScriptableAsNumericScriptableValue(thisObj);
    ScriptableValue secondOperand = NumericMethods.getFirstArgumentAsNumericScriptableValue(args);
    Number firstNumber = (Number) firstOperand.getValue().getValue();
    Number secondNumber = (Number) secondOperand.getValue().getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Long) firstNumber <= (Long) secondNumber));
    } else if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Long) firstNumber <= (Double) secondNumber));
    } else if(firstOperand.getValueType().equals(DecimalType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Double) firstNumber <= (Long) secondNumber));
    } else {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Double) firstNumber <= (Double) secondNumber));
    }
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link BooleanType} indicating if the first parameter is equal to the
   * second parameter. If either parameters are null, then false is returned. Both parameters must either both be
   * numeric, both be BooleanType or both be TextType.
   * 
   * <pre>
   *   $('NumberVarOne').equals($('NumberVarTwo'))
   *   $('BooleanVarOne').equals($('BooleanVarTwo'))
   *   $('TextVarOne').equals($('TextVarTwo'))
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of a numeric type,
   * BooleanType or TextType.
   */
  public static ScriptableValue equals(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue firstOperand = (ScriptableValue) thisObj;
    if(firstOperand.getValue().isNull()) return new ScriptableValue(thisObj, BooleanType.get().falseValue());
    if(args != null && args.length > 0 && args[0] instanceof ScriptableValue) {
      ScriptableValue secondOperand = (ScriptableValue) args[0];
      if(firstOperand.getValueType().isNumeric() && secondOperand.getValueType().isNumeric()) {
        return numericEquals(thisObj, firstOperand, secondOperand);
      } else if(firstOperand.getValueType().equals(BooleanType.get()) && secondOperand.getValueType().equals(BooleanType.get())) {
        return booleanEquals(thisObj, firstOperand, secondOperand);
      } else if(firstOperand.getValueType().equals(TextType.get()) && secondOperand.getValueType().equals(TextType.get())) {
        return textEquals(thisObj, firstOperand, secondOperand);
      } else {
        throw new MagmaJsEvaluationRuntimeException("Cannot invoke equals() with argument of type '" + firstOperand.getValueType().getName() + "' and '" + secondOperand.getValueType().getName() + "'.");
      }
    } else {
      return new ScriptableValue(thisObj, BooleanType.get().falseValue());
    }
  }

  private static ScriptableValue numericEquals(Scriptable thisObj, ScriptableValue firstOperand, ScriptableValue secondOperand) {
    Number firstNumber = (Number) firstOperand.getValue().getValue();
    Number secondNumber = (Number) secondOperand.getValue().getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf(((Long) firstNumber).equals(((Long) secondNumber))));
    } else if(firstOperand.getValueType().equals(IntegerType.get()) && secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf(((Long) firstNumber).doubleValue() == (Double) secondNumber));
    } else if(firstOperand.getValueType().equals(DecimalType.get()) && secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf((Double) firstNumber == ((Long) secondNumber).doubleValue()));
    } else {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf(((Double) firstNumber).equals(((Double) secondNumber))));
    }
  }

  private static ScriptableValue booleanEquals(Scriptable thisObj, ScriptableValue firstOperand, ScriptableValue secondOperand) {
    Boolean firstBoolean = (Boolean) firstOperand.getValue().getValue();
    Boolean secondBoolean = (Boolean) secondOperand.getValue().getValue();
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(firstBoolean.equals(secondBoolean)));
  }

  private static ScriptableValue textEquals(Scriptable thisObj, ScriptableValue firstOperand, ScriptableValue secondOperand) {
    String firstString = (String) firstOperand.getValue().getValue();
    String secondString = (String) secondOperand.getValue().getValue();
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(firstString.equals(secondString)));
  }
}
