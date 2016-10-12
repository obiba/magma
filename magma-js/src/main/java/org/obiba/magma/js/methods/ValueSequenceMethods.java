/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import org.mozilla.javascript.Callable;
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
import com.google.common.collect.Lists;

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
   * <p/>
   * <pre>
   *   $('SequenceVar').first()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue first(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return valueSequence.getSize() > 0 //
          ? new ScriptableValue(thisObj, valueSequence.get(0)) //
          : new ScriptableValue(thisObj, valueSequence.getValueType().nullValue());
    } else {
      return sv;
    }
  }

  /**
   * Returns the last Value of the {@link ValueSequence}. Returns null if the operand is null or the ValueSequence
   * contains no Values.
   * <p/>
   * <pre>
   *   $('SequenceVar').last()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue last(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return valueSequence.getSize() > 0 //
          ? new ScriptableValue(thisObj, valueSequence.get(valueSequence.getSize() - 1)) //
          : new ScriptableValue(thisObj, valueSequence.getValueType().nullValue());
    } else {
      return sv;
    }
  }

  /**
   * Returns the size of the {@link ValueSequence}. Returns null if the operand is null, 1 if the value is not a sequence.
   * <p/>
   * <pre>
   *   $('SequenceVar').size()
   * </pre>
   */
  public static ScriptableValue size(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, IntegerType.get().nullValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(valueSequence.getSize()));
    } else {
      return new ScriptableValue(thisObj, IntegerType.get().valueOf(1));
    }
  }

  /**
   * Returns the {@link Value} of the {@link ValueSequence} specified by the provided index. Returns null if the operand
   * is null. Returns null if index is not an {@link Integer}. Returns null if the index is out of bounds.
   * <p/>
   * <pre>
   *   $('SequenceVar').valueAt(0)
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue valueAt(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValueType().nullValue());
    }
    Integer index = null;
    if(args != null && args.length > 0 && args[0] instanceof Number) {
      index = ((Number) args[0]).intValue();
    } else {
      return new ScriptableValue(thisObj, sv.getValueType().nullValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return valueSequence.getSize() > index //
          ? new ScriptableValue(thisObj, valueSequence.get(index)) //
          : new ScriptableValue(thisObj, valueSequence.getValueType().nullValue());
    } else {
      return index == 0 ? sv : new ScriptableValue(thisObj, sv.getValueType().nullValue());
    }
  }

  /**
   * Returns the {@link ValueSequence} sorted in natural order (default behavior), or using a custom sorting algorithm
   * (javascript function) if specified. Note that some {@link ValueType}s such as {@link BinaryType} and
   * {@link LocaleType} do not have a natural sort order and {@code ValueSequence}s of those types will not be modified
   * when using the default behavior.
   * <p/>
   * <pre>
   *   $('SequenceVar').sort()
   *   $('SequenceVar').sort(function(first, second) {
   *      return first.value() - second.value()
   *    })
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue sort(final Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) throws MagmaJsEvaluationRuntimeException {

    final ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValue());
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();

      ValueSequence sortedValueSequence = null;
      if(args != null && args.length > 0 && args[0] instanceof Function) {
        // Sort using a custom Comparator (javascript function)
        final Callable func = (Callable) args[0];
        sortedValueSequence = valueSequence.sort(new Comparator<Value>() {
          @Override
          public int compare(Value o1, Value o2) {
            return ((Number) func.call(ctx, sv.getParentScope(), sv,
                new ScriptableValue[] { new ScriptableValue(sv, o1), new ScriptableValue(sv, o2) })).intValue();
          }
        });
      } else {
        // Sort based on natural order
        sortedValueSequence = valueSequence.sort();
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
   * <p/>
   * <pre>
   *   $('SequenceVar').avg()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence of numeric values.
   */
  public static ScriptableValue avg(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, DecimalType.get().nullValue());
    }
    if(!sv.getValueType().isNumeric()) {
      throw new MagmaJsEvaluationRuntimeException(
          "Operand to avg() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(thisObj, DecimalType.get().valueOf(NumericMethods.average(valueSequence)),
          sv.getUnit());
    } else {
      // Average of a single value is the value itself.
      return sv;
    }
  }

  /**
   * Returns the standard deviation of the {@link Value}s contained in the {@link ValueSequence}. Returns null if the
   * operand is null or the ValueSequence is empty or contains at least one null value or a non-numeric value.
   * <p/>
   * <pre>
   *   $('SequenceVar').stddev()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence of numeric values.
   */
  public static ScriptableValue stddev(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, DecimalType.get().nullValue());
    }
    if(!sv.getValueType().isNumeric()) {
      throw new MagmaJsEvaluationRuntimeException(
          "Operand to stddev() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }
    if(sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(thisObj, DecimalType.get().valueOf(NumericMethods.stddev(valueSequence)),
          sv.getUnit());
    } else {
      // standard deviation of a single value is 0
      return new ScriptableValue(thisObj, DecimalType.get().valueOf(0), sv.getUnit());
    }
  }

  /**
   * Returns the sum of the {@link Value}s contained in the {@link ValueSequence}. Returns null if the operand is null
   * or the ValueSequence is empty or contains at least one null value. This method throws an exception if the operand
   * is non-numeric.
   * <p/>
   * <pre>
   *   $('SequenceVar').sum()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence of numeric values.
   */
  public static ScriptableValue sum(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(!sv.getValueType().isNumeric()) {
      throw new MagmaJsEvaluationRuntimeException(
          "Operand to sum() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
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

  public static ScriptableValue push(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    ValueType targetType = sv.getValueType();
    Iterable<Value> sequence;
    Value svValue = sv.getValue();
    if(svValue.isNull()) {
      return new ScriptableValue(thisObj, targetType.nullSequence());
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

    return new ScriptableValue(thisObj, targetType.sequenceOf(Lists.newArrayList(sequence)));
  }

  /**
   * Returns a sequence of values, where each value is the transformation of a tuple of values, the i-th tuple contains
   * the i-th element from each of the argument sequences. The returned list length is the length of the longest
   * argument sequence (shortest argument sequence values are null). Not sequential arguments have their value repeated
   * in each tuple.
   * <p/>
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
  public static ScriptableValue zip(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(args == null || args.length == 0) {
      return sv;
    }

    // Extract values, the transformation function and the max sequence length

    List<Object> values = new ArrayList<>();
    values.add(sv.getValue());
    int length = getValueSize(sv.getValue());
    Function func = null;
    for(Object arg : args) {
      if(arg instanceof ScriptableValue) {
        Value value = ((ScriptableValue) arg).getValue();
        values.add(value);
        length = Math.max(length, getValueSize(value));
      } else if(arg instanceof Function) {
        func = (Function) arg;
      } else if(arg != null) {
        values.add(arg);
        length = Math.max(length, 1);
      }
    }

    if(func == null) {
      throw new IllegalArgumentException("Zip requires a transform function.");
    }

    if(length == 0) {
      return new ScriptableValue(sv, sv.getValueType().nullSequence());
    }

    // Transform value tuples to build a value sequence
    ValueType rValueType = null;
    Collection<Value> rvalues = new ArrayList<>();
    for(int i = 0; i < length; i++) {
      Value fvalue = asValue(func.call(ctx, sv.getParentScope(), sv, getTupleAsArguments(sv, values, i)));
      rValueType = fvalue.getValueType();
      rvalues.add(fvalue);
    }

    //noinspection ConstantConditions
    return new ScriptableValue(sv, rValueType.sequenceOf(rvalues));
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
    return seq.isNull() || i >= seq.getSize() ? seq.getValueType().nullValue() : seq.get(i);
  }

  /**
   * Joins the text representation of the values in the sequence, using the provided delimiter, prefix and suffix. A
   * null (resp. empty sequence) will return a null (resp. empty) text value.
   * <p/>
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
  public static ScriptableValue join(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, TextType.get().nullValue());
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
    return new ScriptableValue(sv, TextType.get().valueOf(rval));
  }

  private static ScriptableValue joinValue(ScriptableValue sv, String delimiter, String prefix, String suffix) {
    Value value = sv.getValue();
    if(value.isNull()) {
      return new ScriptableValue(sv, TextType.get().nullValue());
    } else {
      String rval = sv.toString();
      if(rval != null && !rval.isEmpty()) {
        rval = prefix + rval + suffix;
      }
      return new ScriptableValue(sv, TextType.get().valueOf(rval));
    }
  }

  private static String getArgumentAsString(Object[] args, int idx) {
    return args == null || args.length <= idx || args[idx] == null ? "" : args[idx].toString();
  }
}
