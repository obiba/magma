package org.obiba.magma.js.methods;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

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
      throw new MagmaJsEvaluationRuntimeException("Operand to valueAt() method must be a ScriptableValue containing a ValueSequence.");
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
      // Sorting a single value produces that value.
      return sv;
    }
  }

  /**
   * Returns the average of the {@link Value}s contained in the {@link ValueSequence}. Returns null if the operand is
   * null or the ValueSequence is empty or contains at least one null value or a non-numeric value.
   * 
   * <pre>
   *   $('SequenceVar').avg()
   * </pre>
   * 
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence of numeric values.
   */
  public static ScriptableValue avg(final Context ctx, Scriptable thisObj, Object[] args, final Function funObj) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, DecimalType.get().nullValue());
    }
    if(sv.getValueType().isNumeric() == false) {
      throw new MagmaJsEvaluationRuntimeException("Operand to avg() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(thisObj, DecimalType.get().valueOf(NumericMethods.average(valueSequence)), sv.getUnit());
    } else {
      // Average of a single value is the value itself.
      return sv;
    }
  }

  /**
   * Returns the sum of the {@link Value}s contained in the {@link ValueSequence}. Returns null if the operand is null
   * or the ValueSequence is empty or contains at least one null value. This method throws an exception if the operand
   * is non-numeric.
   * 
   * <pre>
   *   $('SequenceVar').sum()
   * </pre>
   * 
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence of numeric values.
   */
  public static ScriptableValue sum(final Context ctx, Scriptable thisObj, Object[] args, final Function funObj) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValueType().isNumeric() == false) {
      throw new MagmaJsEvaluationRuntimeException("Operand to sum() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValueType().nullValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(thisObj, sv.getValueType().valueOf(NumericMethods.sum(valueSequence)), sv.getUnit());
    } else {
      return sv;
    }
  }

  public static ScriptableValue push(final Context ctx, Scriptable thisObj, Object[] args, final Function funObj) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    ValueType targetType = sv.getValueType();
    Iterable<Value> sequence;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, targetType.nullSequence());
    }

    if(sv.getValue().isSequence()) {
      sequence = sv.getValue().asSequence().getValue();
    } else {
      sequence = ImmutableList.of(sv.getValue());
    }

    for(Object argument : args) {
      Value value;
      if(argument instanceof ScriptableValue) {
        value = ((ScriptableValue) argument).getValue();
      } else {
        value = targetType.valueOf(argument);
      }

      if(value.getValueType() != targetType) {
        value = targetType.convert(value);
      }

      if(value.isNull()) {
        sequence = Iterables.concat(sequence, ImmutableList.of(targetType.nullValue()));
      } else {
        if(value.isSequence()) {
          sequence = Iterables.concat(sequence, value.asSequence().getValue());
        } else {
          sequence = Iterables.concat(sequence, ImmutableList.of(value));
        }
      }

    }

    return new ScriptableValue(thisObj, targetType.sequenceOf(sequence));
  }

  /**
   * Returns a sequence of values, where each value is the transformation of a tuple of values, the i-th tuple contains
   * the i-th element from each of the argument sequences. The returned list length is the length of the longest
   * argument sequence (shortest argument sequence values are null). Not sequential arguments have their value repeated
   * in each tuple.
   * 
   * <pre>
   *   // returns "a1, b2, c3"
   *   $('SequenceVarAZ').zip($('SequenceVar19'), function(o1,o2) {
   *     return o1.concat(o2);
   *   })
   *   // returns "afoo1, bfoo2, cfoo3"
   *   $('SequenceVarAZ').zip($('FooVar'), $('SequenceVar19'), function(o1,o2,o3) {
   *     return o1.concat(o2,o3);
   *   })
   * </pre>
   * 
   * @return an instance of {@code ScriptableValue}
   */
  public static ScriptableValue zip(final Context ctx, Scriptable thisObj, Object[] args, final Function funObj) throws MagmaJsEvaluationRuntimeException {
    final ScriptableValue sv = (ScriptableValue) thisObj;
    if(args == null || args.length == 0) {
      return sv;
    }

    // Extract values, the transformation function and the max sequence length
    List<Object> values = new ArrayList<Object>();
    values.add(sv.getValue());
    Function func = null;
    int length = 0;
    for(Object arg : args) {
      if(arg instanceof ScriptableValue) {
        Value value = ((ScriptableValue) arg).getValue();
        values.add(value);
        int size = value.isNull() ? 0 : 1;
        if(value.isNull() == false && value.isSequence()) {
          size = value.asSequence().getSize();
        }
        length = Math.max(length, size);
      } else if(arg instanceof Function) {
        func = (Function) arg;
      } else {
        values.add(arg);
      }
    }

    if(func == null) {
      throw new IllegalArgumentException("Zip requires a transform function.");
    }

    if(length == 0) {
      return new ScriptableValue(sv, sv.getValueType().nullValue());
    }

    // Transform value tuples to build a value sequence
    ValueType rValueType = null;
    List<Value> rvalues = new ArrayList<Value>();
    for(int i = 0; i < length; i++) {
      Value fvalue = asValue(func.call(ctx, sv.getParentScope(), sv, getTupleAsArguments(sv, values, i)));
      rValueType = fvalue.getValueType();
      rvalues.add(fvalue);
    }

    return new ScriptableValue(sv, rValueType.sequenceOf(rvalues));
  }

  private static Value asValue(Object obj) {
    Value value;
    if(obj instanceof ScriptableValue) {
      value = ((ScriptableValue) obj).getValue();
    } else {
      value = ValueType.Factory.newValue(obj);
    }
    return value;
  }

  private static Object[] getTupleAsArguments(ScriptableValue sv, List<Object> values, int i) {
    Object[] objects = new Object[values.size()];
    for(int j = 0; j < values.size(); j++) {
      Object obj = values.get(j);
      if(obj instanceof Value) {
        Value value = (Value) obj;
        if(value.isSequence()) {
          value = getValueAt(value.asSequence(), i);
        }
        objects[j] = new ScriptableValue(sv, value);
      } else {
        objects[j] = obj;
      }
    }
    return objects;
  }

  private static Value getValueAt(ValueSequence seq, int i) {
    if(seq.isNull() || i >= seq.getSize()) {
      return seq.getValueType().nullValue();
    } else {
      return seq.get(i);
    }
  }

  /**
   * Joins the text representation of the values in the sequence, using the provided delimiter, prefix and suffix. A
   * null (resp. empty sequence) will return a null (resp. empty) text value.
   * 
   * <pre>
   *   // returns "1, 2, 3"
   *   $('SequenceVar').join(', ')
   *   // returns "[1, 2, 3]"
   *   $('SequenceVar').join(', ','[',']')
   *   // returns "123"
   *   $('SequenceVar').join()
   * </pre>
   * 
   * @return an instance of {@code ScriptableValue}
   */
  public static ScriptableValue join(final Context ctx, Scriptable thisObj, Object[] args, final Function funObj) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, TextType.get().nullValue());
    }
    String delimiter = getArgumentAsString(args, 0);
    String prefix = getArgumentAsString(args, 1);
    String suffix = getArgumentAsString(args, 2);

    if(sv.getValue().isSequence()) {
      return joinValueSequence(sv, delimiter, prefix, suffix);
    } else {
      return joinValue(sv, delimiter, prefix, suffix);
    }
  }

  private static ScriptableValue joinValueSequence(ScriptableValue sv, String delimiter, String prefix, String suffix) {
    ValueSequence valueSequence = sv.getValue().asSequence();
    String rval = "";
    if(valueSequence.getSize() > 0) {
      StringBuffer buffer = new StringBuffer(prefix);
      for(int i = 0; i < valueSequence.getSize(); i++) {
        buffer.append(valueSequence.get(i).toString());
        if(i < valueSequence.getSize() - 1) {
          buffer.append(delimiter);
        }
      }
      buffer.append(suffix);
      rval = buffer.toString();
    }
    return new ScriptableValue(sv, TextType.get().valueOf(rval));
  }

  private static ScriptableValue joinValue(ScriptableValue sv, String delimiter, String prefix, String suffix) {
    Value value = sv.getValue();
    if(value.isNull()) {
      return new ScriptableValue(sv, TextType.get().nullValue());
    } else {
      String rval = sv.toString();
      if(rval != null && rval.isEmpty() == false) {
        rval = prefix + rval + suffix;
      }
      return new ScriptableValue(sv, TextType.get().valueOf(rval));
    }
  }

  private static String getArgumentAsString(Object[] args, int idx) {
    return args == null || args.length <= idx || args[idx] == null ? "" : args[idx].toString();
  }
}
