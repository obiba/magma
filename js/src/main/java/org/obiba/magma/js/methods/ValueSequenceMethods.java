package org.obiba.magma.js.methods;

import java.util.Comparator;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LocaleType;

/**
 * JavaScript methods that operate on {@link ValueSequence} objects wrapped in {@link ScriptableValue} objects.
 */
public class ValueSequenceMethods {

  /**
   * Returns the first Value of the {@link ValueSequence}. Returns null if the operand is null or the ValueSequence
   * contains no Values.
   * 
   * <pre>
   *   $('SequenceVar').first()
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue first(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      if(valueSequence.getSize() > 0) {
        return new ScriptableValue(thisObj, valueSequence.get(0));
      } else {
        return new ScriptableValue(thisObj, valueSequence.getValueType().nullValue()); // Index out of bounds.
      }
    } else {
      throw new MagmaJsEvaluationRuntimeException("Operand to first() method must be a ScriptableValue containing a ValueSequence.");
    }
  }

  /**
   * Returns the last Value of the {@link ValueSequence}. Returns null if the operand is null or the ValueSequence
   * contains no Values.
   * 
   * <pre>
   *   $('SequenceVar').last()
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue last(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      if(valueSequence.getSize() > 0) {
        return new ScriptableValue(thisObj, valueSequence.get(valueSequence.getSize() - 1));
      } else {
        return new ScriptableValue(thisObj, valueSequence.getValueType().nullValue()); // Index out of bounds.
      }
    } else {
      throw new MagmaJsEvaluationRuntimeException("Operand to last() method must be a ScriptableValue containing a ValueSequence.");
    }
  }

  /**
   * Returns the size of the {@link ValueSequence}. Returns null if the operand is null.
   * 
   * <pre>
   *   $('SequenceVar').size()
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue size(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, IntegerType.get().nullValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(valueSequence.getSize()));
    } else {
      throw new MagmaJsEvaluationRuntimeException("Operand to size() method must be a ScriptableValue containing a ValueSequence.");
    }
  }

  /**
   * Returns the {@link Value} of the {@link ValueSequence} specified by the provided index. Returns null if the operand
   * is null. Returns null if index is not an {@link Integer}. Returns null if the index is out of bounds.
   * 
   * <pre>
   *   $('SequenceVar').valueAt(0)
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue valueAt(Context ctx, Scriptable thisObj, Object[] args, Function funObj) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValue());
    }
    Integer index = null;
    if(args != null && args.length > 0 && args[0] instanceof Number) {
      index = ((Number) args[0]).intValue();
    } else {
      return new ScriptableValue(thisObj, sv.getValueType().nullValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      if(valueSequence.getSize() > index) {
        return new ScriptableValue(thisObj, valueSequence.get(index));
      } else {
        return new ScriptableValue(thisObj, valueSequence.getValueType().nullValue()); // Index out of bounds.
      }
    } else {
      throw new MagmaJsEvaluationRuntimeException("Operand to value() method must be a ScriptableValue containing a ValueSequence.");
    }
  }

  /**
   * Returns the {@link ValueSequence} sorted in natural order (default behavior), or using a custom sorting algorithm
   * (javascript function) if specified. Note that some {@link ValueType}s such as {@link BinaryType} and
   * {@link LocaleType} do not have a natural sort order and {@code ValueSequence}s of those types will not be modified
   * when using the default behavior.
   * 
   * <pre>
   *   $('SequenceVar').sort()
   *   $('SequenceVar').sort(function(first, second) {
   *      return first.value() - second.value()
   *    })
   * </pre>
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue sort(final Context ctx, Scriptable thisObj, Object[] args, final Function funObj) throws MagmaJsEvaluationRuntimeException {

    final ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();

      ValueSequence sortedValueSequence = null;
      if(args != null && args.length > 0 && args[0] instanceof Function) {
        // Sort using a custom Comparator (javascript function)
        final Function func = (Function) args[0];
        sortedValueSequence = (ValueSequence) valueSequence.sort(new Comparator<Value>() {
          @Override
          public int compare(Value o1, Value o2) {
            return ((Number) func.call(ctx, sv.getParentScope(), sv, new ScriptableValue[] { new ScriptableValue(sv, o1), new ScriptableValue(sv, o2) })).intValue();
          }
        });
      } else {
        // Sort based on natural order
        sortedValueSequence = (ValueSequence) valueSequence.sort();
      }
      return new ScriptableValue(thisObj, sortedValueSequence);
    } else {
      throw new MagmaJsEvaluationRuntimeException("Operand to sort() method must be a ScriptableValue containing a ValueSequence.");
    }
  }
}
