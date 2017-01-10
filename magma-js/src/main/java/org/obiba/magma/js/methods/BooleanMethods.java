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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.lang.Booleans;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Methods of the {@code ScriptableValue} javascript class that deal with {@code ScriptableValue} of {@code BooleanType}
 * . Note that other methods that use {@code BooleanType} may be defined elsewhere.
 */
@SuppressWarnings(
    { "UnusedDeclaration", "ChainOfInstanceofChecks" })
public class BooleanMethods {

  private BooleanMethods() {
  }

  /**
   * <pre>
   *   $('Categorical').any('CAT1', 'CAT2')
   * </pre>
   *
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
   *
   * @return true when the value contains all specified parameters, false otherwise. Note that this method will always
   * return false if the value is null.
   */
  public static ScriptableValue all(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj) {
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
        throw new MagmaJsEvaluationRuntimeException(
            "cannot invoke all() with argument of type " + test.getClass().getName());
      }

      if(!sv.contains(testValue)) {
        return buildValue(thisObj, false);
      }
    }
    return buildValue(thisObj, true);
  }

  /**
   * Without arguments, must be applied to boolean values only. With arguments, should be considered as a 'not equals'
   * comparison test.
   * <p/>
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
        if(sv.getValue().isNull()) {
          if(testValue.isNull()) {
            return buildValue(thisObj, false);
          }
        }
        if(sv.contains(testValue)) {
          return buildValue(thisObj, false);
        }
      }
      return buildValue(thisObj, true);
    }
    // Is of form .not()
    return not(ctx, thisObj, funObj);
  }

  /**
   * <pre>
   *   $('BooleanVar').and(someBooleanVar)
   *   $('BooleanVar').and(firstBooleanVar, secondBooleanVar)
   *   $('BooleanVar').and($('OtherBooleanVar'))
   *   $('BooleanVar').and($('OtherBooleanVar').not())
   *   $('BooleanVar').and(someBooleanVar, $('OtherBooleanVar'))
   * </pre>
   */
  @SuppressWarnings("PMD.NcssMethodCount")
  public static ScriptableValue and(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    Value value = sv.getValue();
    if(value.getValueType() != BooleanType.get()) {
      try {
        value = BooleanType.get().convert(value);
      } catch(IllegalArgumentException e) {
        throw new MagmaJsEvaluationRuntimeException(
            "cannot invoke and() for Value of type " + value.getValueType().getName());
      }
    }
    Boolean booleanValue = toBoolean(value);

    if(args == null || args.length == 0) {
      return buildValue(thisObj, booleanValue);
    }

    for(Object arg : args) {
      if(arg instanceof ScriptableValue) {
        ScriptableValue operand = (ScriptableValue) arg;
        booleanValue = Booleans.ternaryAnd(booleanValue, toBoolean(operand.getValue()));
      } else {
        booleanValue = Booleans.ternaryAnd(booleanValue, ScriptRuntime.toBoolean(arg));
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
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(sv.getValue().isNull()));
  }

  /**
   * <pre>
   *   $('BooleanVar').isNotNull()
   * </pre>
   */
  public static ScriptableValue isNotNull(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(!sv.getValue().isNull()));
  }

  /**
   * Returns true {@code BooleanType} if the {@code ScriptableValue} .empty() is operating on is a sequence that
   * contains zero values. Otherwise false is returned.
   * <p/>
   * <pre>
   *   $('Admin.Interview.exportLog.destination').empty()
   * </pre>
   */
  public static ScriptableValue empty(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, BooleanType.get().nullValue());
    }

    if(sv.getValue().isSequence() && sv.getValue().asSequence().getSize() == 0)
      return new ScriptableValue(thisObj, BooleanType.get().trueValue());

    return new ScriptableValue(thisObj, BooleanType.get().falseValue());
  }

  /**
   * <pre>
   *   $('BooleanVar').or(someBooleanVar)
   *   $('BooleanVar').or($('OtherBooleanVar'))
   *   $('BooleanVar').or($('OtherBooleanVar').not())
   * </pre>
   */
  public static ScriptableValue or(Context ctx, Scriptable thisObj, Object[] args, @Nullable Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    Value value = sv.getValue();
    if(value.getValueType() != BooleanType.get()) {
      // try {
      value = BooleanType.get().convert(value);
      // } catch(IllegalArgumentException e) {
      // throw new MagmaJsEvaluationRuntimeException("cannot invoke or() for Value of type " +
      // value.getValueType().getName());
      // }
    }
    Boolean booleanValue = toBoolean(value);

    if(args == null || args.length == 0) {
      return buildValue(thisObj, booleanValue);
    }

    for(Object arg : args) {
      if(arg instanceof ScriptableValue) {
        ScriptableValue operand = (ScriptableValue) arg;
        booleanValue = Booleans.ternaryOr(booleanValue, toBoolean(operand.getValue()));
      } else {
        booleanValue = Booleans.ternaryOr(booleanValue, ScriptRuntime.toBoolean(arg));
      }
      if(Boolean.TRUE.equals(booleanValue)) {
        return buildValue(thisObj, true);
      }
    }
    return buildValue(thisObj, booleanValue);
  }

  /**
   * Returns a new {@link ScriptableValue} of the {@link BooleanType} indicating if the first parameter is equal to the
   * second parameter. If either parameters are null, then false is returned. Both parameters must either both be
   * numeric, both be BooleanType or both be TextType.
   * <p/>
   * <pre>
   *   $('NumberVarOne').eq($('NumberVarTwo'))
   *   $('BooleanVarOne').eq($('BooleanVarTwo'))
   *   $('TextVarOne').eq($('TextVarTwo'))
   * </pre>
   *
   * @throws MagmaJsEvaluationRuntimeException if operands are not ScriptableValue Objects of a numeric type,
   * BooleanType or TextType.
   */
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  public static ScriptableValue eq(Context ctx, Scriptable thisObj, @Nullable Object[] args, @Nullable Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue firstOperand = (ScriptableValue) thisObj;
    if(firstOperand.getValue().isNull()) {
      if(args != null && args.length > 0) {
        if(args[0] instanceof ScriptableValue) {
          ScriptableValue secondOperand = (ScriptableValue) args[0];
          return new ScriptableValue(thisObj, BooleanType.get().valueOf(secondOperand.getValue().isNull()));
        }
        return new ScriptableValue(thisObj, BooleanType.get().valueOf(args[0] == null));
      }
      return new ScriptableValue(thisObj, BooleanType.get().falseValue());
    }

    if(firstOperand.getValue().isSequence()) {
      return new ScriptableValue(thisObj, BooleanType.get().falseValue());
    }

    if(args == null || args.length == 0 || args.length > 0 && args[0] == null) {
      return new ScriptableValue(thisObj, BooleanType.get().falseValue());
    }

    if(args[0] instanceof ScriptableValue) {
      ScriptableValue secondOperand = (ScriptableValue) args[0];
      return eqScriptableValue(thisObj, firstOperand, secondOperand);
    }
    if(firstOperand.getValueType().isNumeric()) {
      return new ScriptableValue(thisObj, NumericMethods.equals(firstOperand, args));
    }
    if(firstOperand.getValueType().equals(TextType.get())) {
      if(args[0] == null) return new ScriptableValue(thisObj, BooleanType.get().nullValue());
      return textEquals(thisObj, firstOperand,
          new ScriptableValue(thisObj, TextType.get().valueOf(args[0].toString())));
    }
    if(firstOperand.getValueType().isDateTime()) {
      return eqDateTime(thisObj, args[0], firstOperand);
    }
    return new ScriptableValue(thisObj, BooleanType.get().falseValue());
  }

  private static ScriptableValue eqDateTime(Scriptable thisObj, Object arg, ScriptableValue firstOperand) {
    if(arg == null) return new ScriptableValue(thisObj, BooleanType.get().nullValue());
    if(firstOperand.getValueType().equals(DateType.get())) {
      return dateEquals(thisObj, firstOperand, new ScriptableValue(thisObj, DateType.get().valueOf(arg)));
    }
    return dateEquals(thisObj, firstOperand, new ScriptableValue(thisObj, DateTimeType.get().valueOf(arg)));
  }

  private static ScriptableValue eqScriptableValue(Scriptable thisObj, ScriptableValue firstOperand,
      ScriptableValue secondOperand) {
    if(firstOperand.getValueType().isNumeric() && secondOperand.getValueType().isNumeric()) {
      return numericEquals(thisObj, firstOperand, secondOperand);
    }
    if(firstOperand.getValueType().equals(BooleanType.get()) &&
        secondOperand.getValueType().equals(BooleanType.get())) {
      return booleanEquals(thisObj, firstOperand, secondOperand);
    }
    if(firstOperand.getValueType().equals(TextType.get()) && secondOperand.getValueType().equals(TextType.get())) {
      return textEquals(thisObj, firstOperand, secondOperand);
    }
    if(firstOperand.getValueType().equals(DateType.get()) && secondOperand.getValueType().equals(DateType.get())) {
      return dateEquals(thisObj, firstOperand, secondOperand);
    }
    if(firstOperand.getValueType().equals(DateTimeType.get()) &&
        secondOperand.getValueType().equals(DateTimeType.get())) {
      return dateTimeEquals(thisObj, firstOperand, secondOperand);
    }
    throw new MagmaJsEvaluationRuntimeException(
        "Cannot invoke equals() with argument of type '" + firstOperand.getValueType().getName() + "' and '" +
            secondOperand.getValueType().getName() + "'.");
  }

  public static ScriptableValue whenNull(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isSequence()) {
      return whenNullSequence(ctx, thisObj, args, funObj);
    }
    if(sv.getValue().isNull() && args != null && args.length > 0) {
      return new ScriptableValue(thisObj, whenNullArgument(sv.getValueType(), args[0]));
    }
    return sv;
  }

  private static ScriptableValue whenNullSequence(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      Value rval = whenNullArgument(sv.getValueType(), args[0]);
      return rval.isSequence() //
          ? new ScriptableValue(thisObj, rval) //
          : new ScriptableValue(thisObj, sv.getValueType().sequenceOf(Collections.singleton(rval)));
    }
    Collection<Value> newValues = new ArrayList<>();
    for(Value val : sv.getValue().asSequence().getValues()) {
      if(val.isNull()) {
        newValues.add(whenNullArgument(val.getValueType(), args[0]));
      } else {
        newValues.add(val);
      }
    }
    return new ScriptableValue(thisObj, sv.getValueType().sequenceOf(newValues));
  }

  private static Value whenNullArgument(ValueType type, Object arg) {
    return arg instanceof ScriptableValue ? ((ScriptableValue) arg).getValue() : type.valueOf(arg);
  }

  private static ScriptableValue numericEquals(Scriptable thisObj, ScriptableValue firstOperand,
      ScriptableValue secondOperand) {
    Value firstOperandValue = firstOperand.getValue();
    Value secondOperandValue = secondOperand.getValue();
    if(firstOperandValue.isNull() || secondOperandValue.isNull()) {
      return new ScriptableValue(thisObj,
          BooleanType.get().valueOf(firstOperandValue.isNull() && secondOperandValue.isNull()));
    }
    Number firstNumber = (Number) firstOperandValue.getValue();
    Number secondNumber = (Number) secondOperandValue.getValue();
    if(firstOperand.getValueType().equals(IntegerType.get()) &&
        secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj, BooleanType.get().valueOf(Objects.equal(firstNumber, secondNumber)));
    }
    if(firstOperand.getValueType().equals(IntegerType.get()) &&
        secondOperand.getValueType().equals(DecimalType.get())) {
      return new ScriptableValue(thisObj,
          BooleanType.get().valueOf(firstNumber.doubleValue() == (Double) secondNumber));
    }
    if(firstOperand.getValueType().equals(DecimalType.get()) &&
        secondOperand.getValueType().equals(IntegerType.get())) {
      return new ScriptableValue(thisObj,
          BooleanType.get().valueOf((Double) firstNumber == secondNumber.doubleValue()));
    }
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(Objects.equal(firstNumber, secondNumber)));
  }

  private static ScriptableValue booleanEquals(Scriptable thisObj, ScriptableValue firstOperand,
      ScriptableValue secondOperand) {
    Value firstOperandValue = firstOperand.getValue();
    Value secondOperandValue = secondOperand.getValue();
    Boolean firstBoolean = firstOperandValue.isNull() ? Boolean.FALSE : (Boolean) firstOperandValue.getValue();
    Boolean secondBoolean = secondOperandValue.isNull() ? Boolean.FALSE : (Boolean) secondOperandValue.getValue();
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(Objects.equal(firstBoolean, secondBoolean)));
  }

  private static ScriptableValue textEquals(Scriptable thisObj, ScriptableValue firstOperand,
      ScriptableValue secondOperand) {
    Value firstOperandValue = firstOperand.getValue();
    String firstString = firstOperandValue.isNull() ? null : (String) firstOperandValue.getValue();
    Value secondOperandValue = secondOperand.getValue();
    String secondString = secondOperandValue.isNull() ? null : (String) secondOperandValue.getValue();
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(Objects.equal(firstString, secondString)));
  }

  private static ScriptableValue dateTimeEquals(Scriptable thisObj, ScriptableValue firstOperand,
      ScriptableValue secondOperand) {
    boolean result = firstOperand.getValue().equals(secondOperand.getValue());
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(result));
  }

  private static ScriptableValue dateEquals(Scriptable thisObj, ScriptableValue firstOperand,
      ScriptableValue secondOperand) {
    boolean result = firstOperand.getValue().equals(secondOperand.getValue());
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(result));
  }

  private static ScriptableValue buildValue(Scriptable scope, @Nullable Boolean value) {
    return value == null
        ? new ScriptableValue(scope, BooleanType.get().nullValue())
        : new ScriptableValue(scope, value ? BooleanType.get().trueValue() : BooleanType.get().falseValue());
  }

  private static ScriptableValue not(Context ctx, Scriptable thisObj, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    Value value = sv.getValue();
    if(value.getValueType() == BooleanType.get()) {
      if(value.isNull()) {
        return new ScriptableValue(thisObj, BooleanType.get().nullValue());
      }

      if(value.isSequence()) {
        // Transform the sequence of Boolean values to a sequence of !values
        Value notSeq = BooleanType.get().sequenceOf(Lists.newArrayList(
            Iterables.transform(value.asSequence().getValue(), new com.google.common.base.Function<Value, Value>() {
              @Override
              public Value apply(Value from) {
                // Transform the input into its invert boolean value
                return BooleanType.get().not(from);
              }

            })));
        return new ScriptableValue(thisObj, notSeq);
      }
      return new ScriptableValue(thisObj, BooleanType.get().not(value));
    }
    throw new MagmaJsEvaluationRuntimeException(
        "cannot invoke not() for Value of type " + value.getValueType().getName());
  }

  @Nullable
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL",
      justification = "Clients expect ternary methods to return null as a valid value.")
  private static Boolean toBoolean(Value value) {
    return value.isNull() ? null : (Boolean) value.getValue();
  }
}
