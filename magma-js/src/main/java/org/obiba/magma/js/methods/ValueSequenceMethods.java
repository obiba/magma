package org.obiba.magma.js.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
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

/**
 * JavaScript methods that operate on {@link ValueSequence} objects wrapped in {@link ScriptableValue} objects.
 */
@SuppressWarnings({ "UnusedDeclaration", "StaticMethodOnlyUsedInOneClass" })
public class ValueSequenceMethods {

  private ValueSequenceMethods() {
  }

  /**
   * Returns the first Value of the {@link ValueSequence}. Returns null if the operand is null or the ValueSequence
   * contains no Values.
   * <p>
   * <pre>
   *   $('SequenceVar').first()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue first(ScriptableValue thisObj, @Nullable Object[] args) {
    ScriptableValue sv = thisObj;

    if(sv.getValue().isNull()) {
      return new ScriptableValue(sv.getValue());
    }

    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();

      return valueSequence.getSize() > 0 //
          ? new ScriptableValue(valueSequence.get(0)) //
          : new ScriptableValue(valueSequence.getValueType().nullValue());
    } else {
      return sv;
    }
  }

  /**
   * Returns the last Value of the {@link ValueSequence}. Returns null if the operand is null or the ValueSequence
   * contains no Values.
   * <p>
   * <pre>
   *   $('SequenceVar').last()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue last(ScriptableValue thisObj, @Nullable Object[] args)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = thisObj;

    if(sv.getValue().isNull()) {
      return new ScriptableValue(sv.getValue());
    }

    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return valueSequence.getSize() > 0 //
          ? new ScriptableValue(valueSequence.get(valueSequence.getSize() - 1)) //
          : new ScriptableValue(valueSequence.getValueType().nullValue());
    } else {
      return sv;
    }
  }

  /**
   * Returns the size of the {@link ValueSequence}. Returns null if the operand is null, 1 if the value is not a sequence.
   * <p>
   * <pre>
   *   $('SequenceVar').size()
   * </pre>
   */
  public static ScriptableValue size(ScriptableValue thisObj, @Nullable Object[] args) {
    ScriptableValue sv = thisObj;

    if(sv.getValue().isNull()) {
      return new ScriptableValue(IntegerType.get().nullValue());
    }

    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(IntegerType.get().valueOf(valueSequence.getSize()));
    } else {
      return new ScriptableValue(IntegerType.get().valueOf(1));
    }
  }

  /**
   * Returns the {@link Value} of the {@link ValueSequence} specified by the provided index. Returns null if the operand
   * is null. Returns null if index is not an {@link Integer}. Returns null if the index is out of bounds.
   * <p>
   * <pre>
   *   $('SequenceVar').valueAt(0)
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue valueAt(ScriptableValue thisObj, Object[] args)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = thisObj;

    if(sv.getValue().isNull()) {
      return new ScriptableValue(sv.getValueType().nullValue());
    }

    Integer index;

    if(args != null && args.length > 0 && args[0] instanceof Number) {
      index = ((Number) args[0]).intValue();
    } else {
      return new ScriptableValue(sv.getValueType().nullValue());
    }

    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return valueSequence.getSize() > index //
          ? new ScriptableValue(valueSequence.get(index)) //
          : new ScriptableValue(valueSequence.getValueType().nullValue());
    } else {
      return index == 0 ? sv : new ScriptableValue(sv.getValueType().nullValue());
    }
  }

  /**
   * Returns the {@link ValueSequence} sorted in natural order (default behavior), or using a custom sorting algorithm
   * (javascript function) if specified. Note that some {@link ValueType}s such as {@link BinaryType} and
   * {@link LocaleType} do not have a natural sort order and {@code ValueSequence}s of those types will not be modified
   * when using the default behavior.
   * <p>
   * <pre>
   *   $('SequenceVar').sort()
   *   $('SequenceVar').sort(function(first, second) {
   *      return first.value() - second.value()
   *    })
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue sort(final ScriptableValue thisObj, @Nullable Object[] args)
      throws MagmaJsEvaluationRuntimeException {
    final ScriptableValue sv = thisObj;

    if(sv.getValue().isNull()) {
      return new ScriptableValue(sv.getValue());
    }

    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      ValueSequence sortedValueSequence;

      if(args != null && args.length > 0 && args[0] instanceof AbstractJSObject &&
          ((AbstractJSObject) args[0]).isFunction()) {
        final AbstractJSObject func = (AbstractJSObject) args[0];

        sortedValueSequence = valueSequence.sort((o1, o2) -> ((Number) func.call(sv,
                new ScriptableValue[] { new ScriptableValue(o1), new ScriptableValue(o2) })).intValue());
      } else {
        sortedValueSequence = valueSequence.sort();
      }

      return new ScriptableValue(sortedValueSequence);
    } else {
      return sv;
    }
  }

  /**
   * Returns the average of the {@link Value}s contained in the {@link ValueSequence}. Returns null if the operand is
   * null or the ValueSequence is empty or contains at least one null value or a non-numeric value.
   * <p>
   * <pre>
   *   $('SequenceVar').avg()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence of numeric values.
   */
  public static ScriptableValue avg(ScriptableValue thisObj, Object[] args) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = thisObj;

    if(sv.getValue().isNull()) {
      return new ScriptableValue(DecimalType.get().nullValue());
    }

    if(!sv.getValueType().isNumeric()) {
      throw new MagmaJsEvaluationRuntimeException(
          "Operand to avg() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }

    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(DecimalType.get().valueOf(NumericMethods.average(valueSequence)), sv.getUnit());
    } else {
      // Average of a single value is the value itself.
      return sv;
    }
  }

  /**
   * Returns the standard deviation of the {@link Value}s contained in the {@link ValueSequence}. Returns null if the
   * operand is null or the ValueSequence is empty or contains at least one null value or a non-numeric value.
   * <p>
   * <pre>
   *   $('SequenceVar').stddev()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence of numeric values.
   */
  public static ScriptableValue stddev(ScriptableValue thisObj, Object[] args)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = thisObj;

    if(sv.getValue().isNull()) {
      return new ScriptableValue(DecimalType.get().nullValue());
    }

    if(!sv.getValueType().isNumeric()) {
      throw new MagmaJsEvaluationRuntimeException(
          "Operand to stddev() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }

    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(DecimalType.get().valueOf(NumericMethods.stddev(valueSequence)), sv.getUnit());
    } else {
      // standard deviation of a single value is 0
      return new ScriptableValue(DecimalType.get().valueOf(0), sv.getUnit());
    }
  }

  /**
   * Returns the sum of the {@link Value}s contained in the {@link ValueSequence}. Returns null if the operand is null
   * or the ValueSequence is empty or contains at least one null value. This method throws an exception if the operand
   * is non-numeric.
   * <p>
   * <pre>
   *   $('SequenceVar').sum()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence of numeric values.
   */
  public static ScriptableValue sum(ScriptableValue thisObj, Object[] args) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = thisObj;

    if(!sv.getValueType().isNumeric()) {
      throw new MagmaJsEvaluationRuntimeException(
          "Operand to sum() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }

    if(sv.getValue().isNull()) {
      return new ScriptableValue(sv.getValueType().nullValue());
    }

    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(sv.getValueType().valueOf(NumericMethods.sum(valueSequence)), sv.getUnit());
    } else {
      return sv;
    }
  }

  public static ScriptableValue push(ScriptableValue thisObj, Object[] args) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = thisObj;
    ValueType targetType = sv.getValueType();
    Iterable<Value> sequence;
    Value svValue = sv.getValue();

    if(svValue.isNull()) {
      return new ScriptableValue(targetType.nullSequence());
    }

    sequence = svValue.isSequence() ? svValue.asSequence().getValue() : ImmutableList.of(svValue);

    for(Object argument : args) {
      Value value = argument instanceof ScriptableValue //
          ? ((ScriptableValue) argument).getValue() //
          : targetType.valueOf(argument);

      if(value.getValueType() != targetType) {
        value = targetType.convert(value);
      }

      if(value.isNull()) {
        sequence = Iterables.concat(sequence, ImmutableList.of(targetType.nullValue()));
      } else {
        sequence = value.isSequence() //
            ? Iterables.concat(sequence, value.asSequence().getValue()) //
            : Iterables.concat(sequence, ImmutableList.of(value));
      }
    }

    return new ScriptableValue(targetType.sequenceOf(Lists.newArrayList(sequence)));
  }

  /**
   * Returns a sequence of values, where each value is the transformation of a tuple of values, the i-th tuple contains
   * the i-th element from each of the argument sequences. The returned list length is the length of the longest
   * argument sequence (shortest argument sequence values are null). Not sequential arguments have their value repeated
   * in each tuple.
   * <p>
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
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  public static ScriptableValue zip(ScriptableValue thisObj, Object[] args) throws MagmaJsEvaluationRuntimeException {

    ScriptableValue sv = thisObj;

    if(args == null || args.length == 0) {
      return sv;
    }

    // Extract values, the transformation function and the max sequence length

    List<Object> values = new ArrayList<>();
    values.add(sv.getValue());
    int length = getValueSize(sv.getValue());
    ScriptObjectMirror func = null;

    for(Object arg : args) {
      if(arg instanceof ScriptableValue) {
        Value value = ((ScriptableValue) arg).getValue();
        values.add(value);
        length = Math.max(length, getValueSize(value));
      } else if(arg instanceof ScriptObjectMirror && ((ScriptObjectMirror)arg).isFunction()) {
        func = ((ScriptObjectMirror) arg);
      } else if(arg != null) {
        values.add(arg);
        length = Math.max(length, 1);
      }
    }

    if(func == null) {
      throw new IllegalArgumentException("Zip requires a transform function.");
    }

    if(length == 0) {
      return new ScriptableValue(sv.getValueType().nullSequence());
    }

    // Transform value tuples to build a value sequence
    ValueType rValueType = null;
    Collection<Value> rvalues = new ArrayList<>();

    for(int i = 0; i < length; i++) {
      Value fvalue = asValue(func.call(null, getTupleAsArguments(values, i)));
      rValueType = fvalue.getValueType();
      rvalues.add(fvalue);
    }

    //noinspection ConstantConditions
    return new ScriptableValue(rValueType.sequenceOf(rvalues));
  }

  private static int getValueSize(Value value) {
    int size = value.isNull() ? 0 : 1;
    if(!value.isNull() && value.isSequence()) {
      size = value.asSequence().getSize();
    }
    return size;
  }

  private static Value asValue(Object obj) {
    return obj instanceof ScriptableValue //
        ? ((ScriptableValue) obj).getValue() //
        : ValueType.Factory.newValue((Serializable) obj);
  }

  private static Object[] getTupleAsArguments(List<Object> values, int i) {
    Object[] objects = new Object[values.size()];
    for(int j = 0; j < values.size(); j++) {
      Object obj = values.get(j);
      if(obj instanceof Value) {
        Value value = (Value) obj;
        if(value.isSequence()) {
          value = getValueAt(value.asSequence(), i);
        }
        objects[j] = new ScriptableValue(value);
      } else {
        objects[j] = obj;
      }
    }
    return objects;
  }

  private static Value getValueAt(ValueSequence seq, int i) {
    return seq.isNull() || i >= seq.getSize() ? seq.getValueType().nullValue() : seq.get(i);
  }

  /**
   * Joins the text representation of the values in the sequence, using the provided delimiter, prefix and suffix. A
   * null (resp. empty sequence) will return a null (resp. empty) text value.
   * <p>
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
  public static ScriptableValue join(ScriptableValue thisObj, Object[] args) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = thisObj;

    if(sv.getValue().isNull()) {
      return new ScriptableValue(TextType.get().nullValue());
    }
    String delimiter = getArgumentAsString(args, 0);
    String prefix = getArgumentAsString(args, 1);
    String suffix = getArgumentAsString(args, 2);

    return sv.getValue().isSequence() //
        ? joinValueSequence(sv, delimiter, prefix, suffix) //
        : joinValue(sv, delimiter, prefix, suffix);
  }

  private static ScriptableValue joinValueSequence(ScriptableValue sv, String delimiter, String prefix, String suffix) {
    ValueSequence valueSequence = sv.getValue().asSequence();
    String rval = "";

    if(valueSequence.getSize() > 0) {
      StringBuilder buffer = new StringBuilder(prefix);
      for(int i = 0; i < valueSequence.getSize(); i++) {
        buffer.append(valueSequence.get(i));
        if(i < valueSequence.getSize() - 1) {
          buffer.append(delimiter);
        }
      }

      buffer.append(suffix);
      rval = buffer.toString();
    }

    return new ScriptableValue(TextType.get().valueOf(rval));
  }

  private static ScriptableValue joinValue(ScriptableValue sv, String delimiter, String prefix, String suffix) {
    Value value = sv.getValue();

    if(value.isNull()) {
      return new ScriptableValue(TextType.get().nullValue());
    } else {
      String rval = sv.toString();
      if(rval != null && !rval.isEmpty()) {
        rval = prefix + rval + suffix;
      }
      return new ScriptableValue(TextType.get().valueOf(rval));
    }
  }

  private static String getArgumentAsString(Object[] args, int idx) {
    return args == null || args.length <= idx || args[idx] == null ? "" : args[idx].toString();
  }
}
