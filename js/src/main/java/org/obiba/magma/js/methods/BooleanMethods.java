package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;

import com.google.common.collect.Iterables;

/**
 * Methods of the {@code ScriptableValue} javascript class that returns {@code ScriptableValue} of {@code BooleanType}
 */
public class BooleanMethods {

  /**
   * <pre>
   *   $('Categorical').any('CAT1', 'CAT2')
   * </pre>
   */
  public static ScriptableValue any(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, BooleanType.get().nullValue());
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

  private static ScriptableValue buildValue(Scriptable scope, boolean value) {
    return new ScriptableValue(scope, value ? BooleanType.get().trueValue() : BooleanType.get().falseValue());
  }
}
