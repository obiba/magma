package org.obiba.magma.js.methods;

import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
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

    Value value = sv.getValue();
    Set<String> values = ImmutableSet.copyOf(Iterables.transform((value.isSequence() ? value.asSequence().getValue() : ImmutableSet.of(value)), Functions.toStringFunction()));
    for(Object test : args) {
      if(values.contains(test.toString())) {
        return buildValue(thisObj, true);
      }
    }
    return buildValue(thisObj, false);
  }

  /**
   * <pre>
   *   $('BooleanVar').not()
   *   $('Categorical').not('CAT1', 'CAT2')
   *   $('Categorical').not($('Other Categorical'))
   * </pre>
   */
  public static ScriptableValue not(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(args != null && args.length > 0) {
      // Is of form this.not(value)
      String value = sv.getValue().toString();
      for(Object test : args) {
        if(value.equals(test.toString())) {
          return buildValue(thisObj, false);
        }
      }
      return buildValue(thisObj, true);
    } else {
      // Is of form .not()
      Value value = sv.getValue();
      if(value.getValueType() == BooleanType.get()) {
        return new ScriptableValue(thisObj, BooleanType.get().not(value));
      }
      throw new UnsupportedOperationException();
    }
  }

  private static ScriptableValue buildValue(Scriptable scope, boolean value) {
    return new ScriptableValue(scope, value ? BooleanType.get().trueValue() : BooleanType.get().falseValue());
  }
}
