/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.methods;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.mozilla.javascript.*;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * JavaScript methods that operate on {@link ValueSequence} objects wrapped in {@link ScriptableValue} objects.
 */
@SuppressWarnings({"UnusedDeclaration", "StaticMethodOnlyUsedInOneClass"})
public class ValueSequenceMethods {

  private ValueSequenceMethods() {
  }

  /**
   * Returns whether the Value is a {@link ValueSequence}.
   * <p/>
   * <pre>
   *   $('SequenceVar').isSequence()
   * </pre>
   */
  public static ScriptableValue isSequence(Context ctx, Scriptable thisObj, @Nullable Object[] args,
                                           @Nullable Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(sv.getValue().isSequence()));
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
    if (sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValue());
    }
    if (sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return valueSequence.getSize() > 0 //
          ? new ScriptableValue(thisObj, valueSequence.get(0)) //
          : new ScriptableValue(thisObj, valueSequence.getValueType().nullValue());
    } else {
      return sv;
    }
  }

  /**
   * Returns the first not null Value of the {@link ValueSequence}. Returns null if the operand is null or the ValueSequence
   * contains no Values or if all Values are null.
   * <p/>
   * <pre>
   *   $('SequenceVar').firstNotNull()
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   */
  public static ScriptableValue firstNotNull(Context ctx, Scriptable thisObj, @Nullable Object[] args,
                                             @Nullable Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if (sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValue());
    }
    if (sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return valueSequence.getSize() > 0 //
          ? new ScriptableValue(thisObj, valueSequence.getValues().stream().filter(value -> !value.isNull())
          .findFirst().orElse(valueSequence.getValueType().nullValue())) //
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
    if (sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValue());
    }
    if (sv.getValue().isSequence()) {
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
    if (sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, IntegerType.get().nullValue());
    }
    if (sv.getValue().isSequence()) {
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
    if (sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValueType().nullValue());
    }
    Integer index = null;
    if (args != null && args.length > 0) {
      try {
        index = ((Number) IntegerType.get().valueOf(getRawValue(args[0])).getValue()).intValue();
      } catch (MagmaRuntimeException e) {
        // ignore, will be handled after
      }
    }

    if (index == null) {
      return new ScriptableValue(thisObj, sv.getValueType().nullValue());
    }
    if (sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return valueSequence.getSize() > index && index >= 0 //
          ? new ScriptableValue(thisObj, valueSequence.get(index)) //
          : new ScriptableValue(thisObj, valueSequence.getValueType().nullValue());
    } else {
      return index == 0 ? sv : new ScriptableValue(thisObj, sv.getValueType().nullValue());
    }
  }

  /**
   * Get the position of the given value in the sequence. Return -1 value if not found.
   * <p/>
   * <pre>
   *   $('SequenceVar').indexOf(0)
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   * @throws MagmaJsEvaluationRuntimeException
   */
  public static ScriptableValue indexOf(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    int index = -1;
    if (args != null && args.length > 0) {
      Value testValue = sv.getValueType().valueOf(getRawValue(args[0]));
      index = sv.indexOf(testValue);
    }
    return new ScriptableValue(thisObj, IntegerType.get().valueOf(index));
  }

  private static Object getRawValue(Object object) {
    Object raw = object;
    if (raw instanceof ScriptableValue) {
      raw = ((ScriptableValue) raw).getValue();
    }
    if (raw instanceof Value) {
      Value value = (Value) raw;
      raw = value.isNull() ? null : value.getValue();
    }
    return raw;
  }

  /**
   * Get the last position of the given value in the sequence. Return -1 value if not found.
   * <p/>
   * <pre>
   *   $('SequenceVar').lastIndexOf(0)
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   * @throws MagmaJsEvaluationRuntimeException
   */
  public static ScriptableValue lastIndexOf(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    int index = -1;
    if (args != null && args.length > 0) {
      Value testValue = sv.getValueType().valueOf(getRawValue(args[0]));
      index = sv.lastIndexOf(testValue);
    }
    return new ScriptableValue(thisObj, IntegerType.get().valueOf(index));
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
    if (sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValue());
    }
    if (sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();

      ValueSequence sortedValueSequence = null;
      if (args != null && args.length > 0 && args[0] instanceof Function) {
        // Sort using a custom Comparator (javascript function)
        final Callable func = (Callable) args[0];
        sortedValueSequence = valueSequence.sort(new Comparator<Value>() {
          @Override
          public int compare(Value o1, Value o2) {
            return ((Number) func.call(ctx, sv.getParentScope(), sv,
                new ScriptableValue[]{new ScriptableValue(sv, o1), new ScriptableValue(sv, o2)})).intValue();
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
   * Returns the {@link ValueSequence} filtered using a custom predicate algorithm (javascript function returning a boolean value).
   * <p/>
   * <pre>
   *   $('SequenceVar').filter(function(value) {
   *      return value.isNull().not();
   *    })
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue filter(final Context ctx, Scriptable thisObj, @Nullable Object[] args,
                                       @Nullable Function funObj) throws MagmaJsEvaluationRuntimeException {

    final ScriptableValue sv = (ScriptableValue) thisObj;
    if (sv.getValue().isNull() || args == null || args.length == 0)
      return new ScriptableValue(thisObj, sv.getValue());
    final Callable func;
    if (args[0] instanceof Function) func = (Callable) args[0];
    else func = null;

    return filter(thisObj, value -> {
      if (func == null) return false;
      Object predicate = func.call(ctx, sv.getParentScope(), sv,
          new ScriptableValue[]{new ScriptableValue(sv, value)});
      if (predicate instanceof ScriptableValue)
        predicate = ((ScriptableValue) predicate).getValue().getValue();
      return (predicate instanceof Boolean) ? (Boolean) predicate : false;
    });
  }


  /**
   * Returns the {@link ValueSequence} after removing starting and ending null values.
   * <p/>
   * <pre>
   *   $('SequenceVar').trim()
   *   // is equivalent to
   *   $('SequenceVar').filter(function(value) {
   *      return value.isNull().not();
   *    })
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence.
   */
  public static ScriptableValue trim(final Context ctx, Scriptable thisObj, @Nullable Object[] args,
                                       @Nullable Function funObj) throws MagmaJsEvaluationRuntimeException {
    return filter(thisObj, value -> !value.isNull());
  }


  private static ScriptableValue filter(Scriptable thisObj, Predicate<Value> predicate) throws MagmaJsEvaluationRuntimeException {
    final ScriptableValue sv = (ScriptableValue) thisObj;
    if (sv.getValue().isNull() || predicate == null)
      return new ScriptableValue(thisObj, sv.getValue());
    List<Value> originalValues;
    if (sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      originalValues = valueSequence.isNull() ? Lists.newArrayList() : valueSequence.getValues();
    } else {
      Value value = sv.getValue();
      originalValues = value.isNull() ? Lists.newArrayList() : Lists.newArrayList(value);
    }

    List<Value> filteredValues = originalValues.stream().filter(predicate).collect(Collectors.toList());
    ValueSequence filteredValueSequence = filteredValues.isEmpty() ?
        sv.getValueType().nullSequence() : sv.getValueType().sequenceOf(filteredValues);
    return new ScriptableValue(thisObj, filteredValueSequence);
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
    if (sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, DecimalType.get().nullValue());
    }
    if (!sv.getValueType().isNumeric()) {
      throw new MagmaJsEvaluationRuntimeException(
          "Operand to avg() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }
    if (sv.getValue().isSequence()) {
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
   * operand is null or the ValueSequence is empty or a non-numeric value.
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
    if (sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, DecimalType.get().nullValue());
    }
    if (!sv.getValueType().isNumeric()) {
      throw new MagmaJsEvaluationRuntimeException(
          "Operand to stddev() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }
    if (sv.getValue().isSequence()) {
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
   * or the ValueSequence is empty. This method throws an exception if the operand
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
    if (!sv.getValueType().isNumeric()) {
      throw new MagmaJsEvaluationRuntimeException(
          "Operand to sum() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }
    if (sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValueType().nullValue());
    }
    if (sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(thisObj, sv.getValueType().valueOf(NumericMethods.sum(valueSequence)), sv.getUnit());
    } else {
      return sv;
    }
  }

  /**
   * Returns the minimum of the {@link Value}s contained in the {@link ValueSequence}. Returns null if the operand is null
   * or the ValueSequence is empty. This method throws an exception if the operand
   * is non-numeric.
   * <p/>
   * <pre>
   *   $('SequenceVar').min()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence of numeric values.
   */
  public static ScriptableValue min(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if (!sv.getValueType().isNumeric()) {
      throw new MagmaJsEvaluationRuntimeException(
          "Operand to min() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }
    if (sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValueType().nullValue());
    }
    if (sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(thisObj, sv.getValueType().valueOf(NumericMethods.min(valueSequence)), sv.getUnit());
    } else {
      return sv;
    }
  }


  /**
   * Returns the maximum of the {@link Value}s contained in the {@link ValueSequence}. Returns null if the operand is null
   * or the ValueSequence is empty. This method throws an exception if the operand
   * is non-numeric.
   * <p/>
   * <pre>
   *   $('SequenceVar').max()
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operand does not contain a ValueSequence of numeric values.
   */
  public static ScriptableValue max(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if (!sv.getValueType().isNumeric()) {
      throw new MagmaJsEvaluationRuntimeException(
          "Operand to max() method must be numeric, but was invoked for '" + sv.getValueType().getName() + "'");
    }
    if (sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, sv.getValueType().nullValue());
    }
    if (sv.getValue().isSequence()) {
      ValueSequence valueSequence = sv.getValue().asSequence();
      return new ScriptableValue(thisObj, sv.getValueType().valueOf(NumericMethods.max(valueSequence)), sv.getUnit());
    } else {
      return sv;
    }
  }

  /**
   * Push a value to a value to produce a value sequence. Also accepts a value sequence as input, in which case, both sequences
   * are concatenated to produce a single one (it does not produce a sequence of sequence).
   * <p>
   * <p>If the value being added is not of the same type as the sequence, it will be converted to the
   * sequence's type. If the conversion fails, an exception is thrown.</p>
   * <p>
   * <p>If the sequence is null, this method returns a null sequence. If the sequence is empty, this method returns a
   * new sequence containing the parameter(s). If the parameter is null, a null value is appended.</p>
   * <p>
   * <pre>
   *   // Add a value to a sequence, then compute the average of the resulting sequence
   *   $('BloodPressure:Measure.RES_PULSE').push($('StandingHeight:FIRST_RES_PULSE')).avg();
   *
   *   // Aadd several values to a value (or a value sequence)
   *   $('VARX').push(1, 2, 3)
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   * @throws MagmaJsEvaluationRuntimeException
   */
  public static ScriptableValue push(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    ValueType targetType = sv.getValueType();
    Iterable<Value> sequence;
    Value svValue = sv.getValue();
    if (svValue.isNull()) {
      return new ScriptableValue(thisObj, targetType.nullSequence());
    }

    sequence = svValue.isSequence() ? svValue.asSequence().getValue() : ImmutableList.of(svValue);

    for (Object argument : args) {
      Value value = argument instanceof ScriptableValue //
          ? ((ScriptableValue) argument).getValue() //
          : targetType.valueOf(argument);

      if (value.getValueType() != targetType) {
        value = targetType.convert(value);
      }

      if (value.isNull()) {
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
   * Append a value to a value to produce a value sequence. Also accepts a value sequence as input, in which case, both sequences
   * are concatenated to produce a single one (it does not produce a sequence of sequence).
   * <p>
   * <p>If the value being added is not of the same type as the sequence, it will be converted to the
   * sequence's type. If the conversion fails, an exception is thrown.</p>
   * <p>
   * <p>If the sequence is null or empty, this method returns a
   * new sequence containing the parameter(s). If the parameter is null, a null value is appended.</p>
   * <p>
   * <pre>
   *   // Append a value to a sequence, then compute the average of the resulting sequence
   *   $('BloodPressure:Measure.RES_PULSE').append($('StandingHeight:FIRST_RES_PULSE')).avg();
   *
   *   // Append several values to a value (or a value sequence)
   *   $('VARX').append(1, 2, 3)
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   * @throws MagmaJsEvaluationRuntimeException
   */
  public static ScriptableValue append(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return pendValue(thisObj, args, true);
  }


  /**
   * Prepend a value to a value to produce a value sequence. Also accepts a value sequence as input, in which case, both sequences
   * are concatenated to produce a single one (it does not produce a sequence of sequence).
   * <p>
   * <p>If the value being added is not of the same type as the sequence, it will be converted to the
   * sequence's type. If the conversion fails, an exception is thrown.</p>
   * <p>
   * <p>If the sequence is null or empty, this method returns a
   * new sequence containing the parameter(s). If the parameter is null, a null value is appended.</p>
   * <p>
   * <pre>
   *   // Prepend a value to a sequence, then compute the average of the resulting sequence
   *   $('BloodPressure:Measure.RES_PULSE').prepend($('StandingHeight:FIRST_RES_PULSE')).avg();
   *
   *   // Prepend several values to a value (or a value sequence)
   *   $('VARX').prepend(1, 2, 3)
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   * @throws MagmaJsEvaluationRuntimeException
   */
  public static ScriptableValue prepend(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    return pendValue(thisObj, args, false);
  }

  private static ScriptableValue pendValue(Scriptable thisObj, Object[] args, boolean append) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    ValueType targetType = sv.getValueType();
    Iterable<Value> originalSequence = asIterableValues(sv.getValue());

    List<Value> pendSequence = prepareValuesToInsert(thisObj, args);

    Iterable<Value> sequence = append ?
        Iterables.concat(originalSequence, pendSequence) :
        Iterables.concat(pendSequence, originalSequence);

    return new ScriptableValue(thisObj, targetType.sequenceOf(Lists.newArrayList(sequence)));
  }

  /**
   * Insert a value to a value to produce a value sequence. Also accepts a value sequence as input, in which case, both sequences
   * are merged to produce a single one (it does not produce a sequence of sequence).
   * <p>
   * <p>If the value being added is not of the same type as the sequence, it will be converted to the
   * sequence's type. If the conversion fails, an exception is thrown.</p>
   * <p>
   * <p>If the sequence is null or empty, this method returns a
   * new sequence containing the parameter(s). If the parameter is null, a null value is appended.</p>
   * <p>
   * <pre>
   *   // Insert a value to a sequence, then compute the average of the resulting sequence
   *   $('BloodPressure:Measure.RES_PULSE').insertAt(0, $('StandingHeight:FIRST_RES_PULSE')).avg();
   *
   *   // Insert several values to a value (or a value sequence)
   *   $('VARX').insertAt(1, 1, 2, 3)
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   * @throws MagmaJsEvaluationRuntimeException
   */
  public static ScriptableValue insertAt(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    if (args == null || args.length < 2) throw new IllegalArgumentException("Wrong insertAt() arguments.");
    int position = ((Number) args[0]).intValue();
    Object[] argValues = new Object[args.length - 1];
    for (int i = 1; i < args.length; i++) {
      argValues[i - 1] = args[i];
    }
    List<Value> insertSequence = prepareValuesToInsert(thisObj, argValues);

    ScriptableValue sv = (ScriptableValue) thisObj;
    ValueType targetType = sv.getValueType();
    Iterable<Value> originalSequence = asIterableValues(sv.getValue());

    List<Value> sequence = Lists.newArrayList();
    int i = 0;
    for (Value value : originalSequence) {
      if (i == position) {
        for (Value insertValue : insertSequence) {
          sequence.add(insertValue);
        }
      }
      sequence.add(value);
      i++;
    }
    while (position >= i) {
      if (i < position) {
        sequence.add(targetType.nullValue());
      } else if (i == position) {
        for (Value insertValue : insertSequence) {
          sequence.add(insertValue);
        }
      }
      i++;
    }

    return new ScriptableValue(thisObj, targetType.sequenceOf(sequence));
  }

  private static Iterable<Value> asIterableValues(Value value) {
    if (value.isNull()) return Lists.newArrayList();
    return value.isSequence() ? value.asSequence().getValue() : ImmutableList.of(value);
  }

  private static List<Value> prepareValuesToInsert(Scriptable thisObj, Object[] args) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    ValueType targetType = sv.getValueType();
    List<Value> insertSequence = Lists.newArrayList();

    for (Object argument : args) {
      Value value = argument instanceof ScriptableValue //
          ? ((ScriptableValue) argument).getValue() //
          : targetType.valueOf(argument);

      if (value.getValueType() != targetType) {
        value = targetType.convert(value);
      }

      if (value.isSequence()) {
        if (!value.isNull()) value.asSequence().getValue().forEach(insertSequence::add);
      } else {
        insertSequence.add(value);
      }
    }
    return insertSequence;
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
  @SuppressWarnings({"OverlyLongMethod", "PMD.NcssMethodCount"})
  public static ScriptableValue zip(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if (args == null || args.length == 0) {
      return sv;
    }

    // Extract values, the transformation function and the max sequence length

    List<Object> values = new ArrayList<>();
    values.add(sv.getValue());
    int length = getValueSize(sv.getValue());
    Function func = null;
    for (Object arg : args) {
      if (arg instanceof ScriptableValue) {
        Value value = ((ScriptableValue) arg).getValue();
        values.add(value);
        length = Math.max(length, getValueSize(value));
      } else if (arg instanceof Function) {
        func = (Function) arg;
      } else if (arg != null) {
        values.add(arg);
        length = Math.max(length, 1);
      }
    }

    if (func == null) {
      throw new IllegalArgumentException("Zip requires a transform function.");
    }

    if (length == 0) {
      return new ScriptableValue(sv, sv.getValueType().nullSequence());
    }

    // Transform value tuples to build a value sequence
    ValueType rValueType = null;
    Collection<Value> rvalues = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      Value fvalue = asValue(func.call(ctx, sv.getParentScope(), sv, getTupleAsArguments(sv, values, i)));
      rValueType = fvalue.getValueType();
      rvalues.add(fvalue);
    }

    //noinspection ConstantConditions
    return new ScriptableValue(sv, rValueType.sequenceOf(rvalues));
  }

  private static int getValueSize(Value value) {
    int size = value.isNull() ? 0 : 1;
    if (!value.isNull() && value.isSequence()) {
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
    for (int j = 0; j < values.size(); j++) {
      Object obj = values.get(j);
      if (obj instanceof Value) {
        Value value = (Value) obj;
        if (value.isSequence()) {
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
    if (sv.getValue().isNull()) {
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
    if (valueSequence.getSize() > 0) {
      StringBuilder buffer = new StringBuilder(prefix);
      for (int i = 0; i < valueSequence.getSize(); i++) {
        buffer.append(valueSequence.get(i));
        if (i < valueSequence.getSize() - 1) {
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
    if (value.isNull()) {
      return new ScriptableValue(sv, TextType.get().nullValue());
    } else {
      String rval = sv.toString();
      if (rval != null && !rval.isEmpty()) {
        rval = prefix + rval + suffix;
      }
      return new ScriptableValue(sv, TextType.get().valueOf(rval));
    }
  }

  private static String getArgumentAsString(Object[] args, int idx) {
    return args == null || args.length <= idx || args[idx] == null ? "" : args[idx].toString();
  }
}
