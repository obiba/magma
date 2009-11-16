package org.obiba.meta.js.methods;

import java.util.Date;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Occurrence;
import org.obiba.meta.Value;
import org.obiba.meta.ValueSet;
import org.obiba.meta.VariableEntity;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.js.ScriptableValue;
import org.obiba.meta.type.DateType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

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

    String name = (String) args[0];
    ValueSet valueSet = (ValueSet) ctx.getThreadLocal(ValueSet.class);
    if(valueSet == null) {
      throw new IllegalStateException("valueSet cannot be null");
    }
    if(name.indexOf(':') < 0) {
      name = valueSet.getCollection().getName() + ':' + name;
    }

    final VariableValueSource source = lookupSource(valueSet.getVariableEntity(), name);

    if(source.getVariable().isRepeatable()) {
      Set<Occurrence> occurrences = valueSet.getCollection().loadOccurrences(valueSet, source.getVariable());
      Iterable<Value> values = Iterables.transform(occurrences, new com.google.common.base.Function<Occurrence, Value>() {
        @Override
        public Value apply(Occurrence from) {
          return source.getValue(from);
        }
      });
      // Return an object that can be indexed (e.g.: $('BP.Systolic')[2] or chained $('BP.Systolic').avg() )
      return new ScriptableValue(thisObj, Iterables.toArray(values, Value.class));
    } else {
      Value value = source.getValue(lookupValueSet(valueSet.getVariableEntity(), source.getVariable().getCollection()));
      return new ScriptableValue(thisObj, value);
    }
  }

  private static VariableValueSource lookupSource(VariableEntity entity, String name) {
    return MetaEngine.get().lookupVariable(entity.getType(), name);
  }

  private static ValueSet lookupValueSet(VariableEntity entity, String collection) {
    return MetaEngine.get().lookupCollection(collection).loadValueSet(entity);
  }
}
