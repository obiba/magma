package org.obiba.magma.js.methods;

import java.util.Date;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.DateType;

import com.google.common.collect.ImmutableSet;

public final class GlobalMethods {

  /**
   * Set of methods to be exposed as top-level methods (ones that can be invoked anywhere)
   */
  public static final Set<String> GLOBAL_METHODS = ImmutableSet.of("valueOf", "now");

  /**
   * Creates an instance of {@code ScriptableValue} containing the current date and time.
   * 
   * @return an instance of {@code ScriptableValue} containing the current date and time.
   */
  public static ScriptableValue now(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return new ScriptableValue(thisObj, DateType.get().valueOf(new Date()));
  }

  /**
   * Allows invoking {@code VariableValueSource#getValue(ValueSet)} and returns a {@code ScriptableValue}. Accessed as $
   * in javascript.
   * 
   * <pre>
   *   $('Participant.firstName')
   *   $('other-collection:SMOKER_STATUS')
   * </pre>
   * @return an instance of {@code ScriptableValue}
   */
  public static Scriptable valueOf(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 1) {
      throw new UnsupportedOperationException("$() expects exactly one argument: a variable name.");
    }

    MagmaContext context = MagmaContext.asMagmaContext(ctx);

    String name = (String) args[0];
    ValueSet valueSet = (ValueSet) context.peek(ValueSet.class);

    // Find the named source
    final VariableValueSource source;
    // Is this a fully qualified name?
    if(name.indexOf(':') < 0) {
      // No, then lookup the source within the ValueSet's Collection
      source = valueSet.getCollection().getVariableValueSource(valueSet.getVariableEntity().getType(), name);
    } else {
      // Yes, then lookup the source within the engine.
      source = lookupSource(valueSet.getVariableEntity(), name);
    }

    // If the source is in a different Collection, then we need to resolve the other ValueSet
    if(source.getVariable().getCollection().equals(valueSet.getCollection().getName()) == false) {
      // Resolve the joined valueSet
      try {
        valueSet = lookupValueSet(valueSet.getVariableEntity(), source.getVariable().getCollection());
      } catch(NoSuchValueSetException e) {
        // Entity does not have a ValueSet in joined collection
        // Return a null value
        return new ScriptableValue(thisObj, source.getVariable().getValueType().nullValue());
      }
    }

    Value value = source.getValue(valueSet);
    return new ScriptableValue(thisObj, value);
  }

  private static VariableValueSource lookupSource(VariableEntity entity, String name) {
    return MagmaEngine.get().lookupVariable(entity.getType(), name);
  }

  private static ValueSet lookupValueSet(VariableEntity entity, String collection) {
    return MagmaEngine.get().lookupCollection(collection).loadValueSet(entity);
  }
}
