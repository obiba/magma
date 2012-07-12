package org.obiba.magma.js.methods;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.js.JavascriptValueSource.VectorCache;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public final class GlobalMethods extends AbstractGlobalMethodProvider {

  private static final Logger log = LoggerFactory.getLogger(GlobalMethods.class);

  /**
   * Set of methods to be exposed as top-level methods (ones that can be invoked anywhere)
   */
  private static final Set<String> GLOBAL_METHODS = ImmutableSet.of("$", "$join", "now", "log", "$var", "$id", "newValue");

  @Override
  protected Set<String> getExposedMethods() {
    return GLOBAL_METHODS;
  }

  /**
   * Creates an instance of {@code ScriptableValue} containing the current date and time.
   * 
   * @return an instance of {@code ScriptableValue} containing the current date and time.
   */
  public static ScriptableValue now(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return new ScriptableValue(thisObj, DateTimeType.get().valueOf(new Date()));
  }

  /**
   * Creates a new value.
   * 
   * <pre>
   *   newValue('Foo')
   *   newValue(123)
   *   newValue('123','integer')
   * </pre>
   * @return an instance of {@code ScriptableValue}
   */
  public static ScriptableValue newValue(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    Object value = args[0];
    Value v;
    if(args.length > 1) {
      v = ValueType.Factory.forName((String) args[1]).valueOf(value);
    } else {
      v = ValueType.Factory.newValue(value);
    }
    return new ScriptableValue(thisObj, v);
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
  public static Scriptable $(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$() expects exactly one argument: a variable name.");
    }

    MagmaContext context = MagmaContext.asMagmaContext(ctx);

    String name = (String) args[0];

    return valueFromContext(context, thisObj, name);
  }

  /**
   * Allows joining a variable value to another variable value that provides a entity identifier. Accessed as $join in
   * javascript.
   * 
   * <pre>
   *   $join('medications.Drugs:BRAND_NAME','MEDICATION_1')
   * </pre>
   * @return an instance of {@code ScriptableValue}
   */
  public static Scriptable $join(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 2) {
      throw new IllegalArgumentException("$join() expects exactly two arguments: the reference the variable to be joined and the name of the variable holding entity identifiers.");
    }

    MagmaContext context = MagmaContext.asMagmaContext(ctx);
    String joinedName = (String) args[0];
    String name = (String) args[1];
    ValueTable valueTable = context.peek(ValueTable.class);
    Value identifier = valueFromContext(context, thisObj, name).getValue();

    // Find the joined named source
    MagmaEngineVariableResolver reference = MagmaEngineVariableResolver.valueOf(joinedName);
    ValueTable joinedTable = reference.resolveTable(valueTable);
    VariableValueSource joinedSource = reference.resolveSource(valueTable);

    // Default value is null if joined table has no valueSet (equivalent to a LEFT JOIN)
    Value value = joinedSource.getVariable().isRepeatable() ? joinedSource.getValueType().nullSequence() : joinedSource.getValueType().nullValue();
    if(identifier.isNull() == false) {
      VariableEntity entity = new VariableEntityBean(joinedTable.getEntityType(), identifier.toString());
      if(joinedTable.hasValueSet(entity)) {
        value = joinedSource.getValue(joinedTable.getValueSet(entity));
      }
    }
    return new ScriptableValue(thisObj, value, joinedSource.getVariable().getUnit());
  }

  public static Scriptable $var(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$var() expects exactly one argument: a variable name.");
    }

    MagmaContext context = MagmaContext.asMagmaContext(ctx);
    String name = (String) args[0];

    MagmaEngineVariableResolver reference = MagmaEngineVariableResolver.valueOf(name);

    VariableValueSource source = null;
    if(context.has(ValueTable.class)) {
      source = reference.resolveSource(context.peek(ValueTable.class));
    } else {
      source = reference.resolveSource();
    }
    return new ScriptableVariable(thisObj, source.getVariable());
  }

  /**
   * Allows accessing the current entity identifier.
   * 
   * <pre>
   * $id()
   * </pre>
   * @return an instance of {@code ScriptableValue}
   */
  public static Scriptable $id(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    MagmaContext context = MagmaContext.asMagmaContext(ctx);
    VariableEntity entity = (VariableEntity) context.peek(VariableEntity.class);
    return new ScriptableValue(thisObj, TextType.get().valueOf(entity.getIdentifier()));
  }

  /**
   * Provides 'info' level logging of messages and variables. Returns a {@code ScriptableValue}. Accessed as 'log' in
   * javascript.
   * 
   * <pre>
   *   log('My message')
   *   log(onyx('org.obiba.onyx.lastExportDate'))
   *   log('The last export date: {}', onyx('org.obiba.onyx.lastExportDate'))
   *   log('The last export date: {} Days before purge: {}', onyx('org.obiba.onyx.lastExportDate'), onyx('org.obiba.onyx.participant.purge'))
   * </pre>
   * @return an instance of {@code ScriptableValue}
   */
  public static Scriptable log(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length < 1) {
      throw new UnsupportedOperationException("log() expects either one or more arguments. e.g. log('message'), log('var 1 {}', $('var1')), log('var 1 {} var 2 {}', $('var1'), $('var2')).");
    }
    if(args.length == 1) {
      if(args[0] instanceof Exception) {
        log.warn("Exception during JS execution", (Exception) args[0]);
      } else {
        log.info(args[0].toString());
      }
    } else {
      log.info(args[0].toString(), Arrays.copyOfRange(args, 1, args.length));
    }
    return thisObj;
  }

  private static ScriptableValue valueFromContext(MagmaContext context, Scriptable thisObj, String name) {
    ValueTable valueTable = context.peek(ValueTable.class);

    MagmaEngineVariableResolver reference = MagmaEngineVariableResolver.valueOf(name);

    // Find the named source
    final VariableValueSource source = reference.resolveSource(valueTable);

    // Test whether this is a vector-oriented evaluation or a ValueSet-oriented evaluation
    if(context.has(VectorCache.class)) {
      return valuesForVector(context, thisObj, reference, source);
    } else {
      return valueForValueSet(context, thisObj, reference, source);
    }
  }

  private static ScriptableValue valuesForVector(MagmaContext context, Scriptable thisObj, MagmaEngineVariableResolver reference, VariableValueSource source) {
    VectorSource vectorSource = source.asVectorSource();
    if(vectorSource == null) {
      throw new IllegalArgumentException("source cannot provide vectors (" + source.getClass().getName() + ")");
    }
    // Load the vector
    VectorCache cache = context.peek(VectorCache.class);
    return new ScriptableValue(thisObj, cache.get(context, vectorSource), source.getVariable().getUnit());
  }

  private static ScriptableValue valueForValueSet(MagmaContext context, Scriptable thisObj, MagmaEngineVariableResolver reference, VariableValueSource source) {
    ValueSet valueSet = (ValueSet) context.peek(ValueSet.class);
    // Tests whether this valueSet is in the same table as the referenced ValueTable
    if(reference.isJoin(valueSet)) {
      // Resolve the joined valueSet
      try {
        valueSet = reference.join(valueSet);
      } catch(NoSuchValueSetException e) {
        // Entity does not have a ValueSet in joined collection
        // Return a null value
        return new ScriptableValue(thisObj, source.getValueType().nullValue(), source.getVariable().getUnit());
      }
    }

    Value value = source.getValue(valueSet);
    return new ScriptableValue(thisObj, value, source.getVariable().getUnit());
  }

}
