package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;

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
}
