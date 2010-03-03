package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

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
        // Index out of bounds.
        return new ScriptableValue(thisObj, TextType.get().nullValue());
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
        // Index out of bounds.
        return new ScriptableValue(thisObj, TextType.get().nullValue());
      }
    } else {
      throw new MagmaJsEvaluationRuntimeException("Operand to first() method must be a ScriptableValue containing a ValueSequence.");
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
      return new ScriptableValue(thisObj, sv.getValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(valueSequence.getSize()));
    } else {
      throw new MagmaJsEvaluationRuntimeException("Operand to first() method must be a ScriptableValue containing a ValueSequence.");
    }
  }
}
