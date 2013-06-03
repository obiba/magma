package org.obiba.magma.js.methods;

import javax.annotation.Nullable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Methods of the {@code ScriptableValue} javascript class that apply to all data ValueTypes.
 */
public class ScriptableValueMethods {

  private static final Logger log = LoggerFactory.getLogger(ScriptableValueMethods.class);

  private ScriptableValueMethods() {}

  /**
   * Returns the javascript value of a {@code ScriptableValue}. Useful to turn BooleanType ScriptableValues into native
   * javascript values to be used inside if/else statements.
   * <p/>
   * <pre>
   *   if($('Admin.Interview.exportLog.destination').empty().value()) {
   *      // true
   *   } else {
   *      // false
   *   }
   * </pre>
   */
  public static Object value(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return null;
    }
    return sv.getDefaultValue(null);
  }

  /**
   * 1) Invoked with no arguments - ex: type()
   * <p/>
   * Returns a new {@code ScriptableValue} of type "text" containing the name of the {@code ValueType}.
   * <p/>
   * 2) Invoked with type argument - ex: type("text")
   * <p/>
   * Performs a {@code ValueType} conversion and returns a new {@code ScriptableValue} of the requested type.
   */
  public static ScriptableValue type(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    ValueType valueType = sv.getValueType();

    // Return the ValueType name
    if(args.length == 0) {
      return new ScriptableValue(thisObj, TextType.get().valueOf(valueType.getName()));
    }
    if(args.length > 1) {
      log.warn("{} extra parameters were passed to the javascript method. These will be ignored.", args.length - 1);
    }
    // Perform a ValueType conversion
    return new ScriptableValue(thisObj, ValueType.Factory.forName(args[0].toString()).convert(sv.getValue()));
  }

  /**
   * Get the value length: length of the string representation (default) or number of bytes for a binary value.
   * If it is a sequence, a sequence of each value length is returned.
   */
  public static ScriptableValue length(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    Value value = sv.getValue();
    if(value.isSequence()) {
      ValueSequence valueSequence = value.asSequence();
      Value rval = valueSequence.isNull()
          ? IntegerType.get().nullSequence()
          : IntegerType.get().sequenceOf(Lists.newArrayList(
              Iterables.transform(valueSequence.getValue(), new com.google.common.base.Function<Value, Value>() {

                @Nullable
                @Override
                public Value apply(@Nullable Value input) {
                  return input == null || input.isNull()
                      ? IntegerType.get().nullValue()
                      : IntegerType.get().valueOf(input.getLength());
                }
              })));
      return new ScriptableValue(sv, rval);
    }
    Value rval = value.isNull() ? IntegerType.get().nullValue() : IntegerType.get().valueOf(value.getLength());
    return new ScriptableValue(thisObj, rval);
  }

  /**
   * Transform a Value into a ValueSequence
   */
  public static ScriptableValue asSequence(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    Value value = sv.getValue();
    return value.isSequence()
        ? sv
        : new ScriptableValue(thisObj, value.getValueType().sequenceOf(Lists.newArrayList(value)));
  }

}
