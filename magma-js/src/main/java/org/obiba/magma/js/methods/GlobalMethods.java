package org.obiba.magma.js.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.JavascriptValueSource.VectorCache;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings(
    { "IfMayBeConditional", "ChainOfInstanceofChecks", "OverlyCoupledClass", "StaticMethodOnlyUsedInOneClass" })
public final class GlobalMethods extends AbstractGlobalMethodProvider {

  private static final Logger log = LoggerFactory.getLogger(GlobalMethods.class);

  /**
   * Set of methods to be exposed as top-level methods (ones that can be invoked anywhere)
   */
  private static final Set<String> GLOBAL_METHODS = ImmutableSet
      .of("$", "$val", "$value", "$created", "$lastupdate", "$this", "$join", "now", "log", "$var", "$variable", "$id",
          "$identifier", "$group", "$groups", "newValue", "newSequence");

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
   * <p/>
   * <pre>
   *   newValue('Foo')
   *   newValue(123)
   *   newValue('123','integer')
   * </pre>
   *
   * @return an instance of {@code ScriptableValue}
   */
  public static ScriptableValue newValue(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    Object value = args[0];
    Value v = args.length > 1
        ? ValueType.Factory.forName((String) args[1]).valueOf(value)
        : ValueType.Factory.newValue((Serializable) value);
    return new ScriptableValue(thisObj, v);
  }

  /**
   * Creates a new value sequence.
   * <p/>
   * <pre>
   *   newSequence('Foo')
   *   newSequence(['Foo', 'Bar'])
   *   newSequence(123)
   *   newSequence('123','integer')
   *   newSequence([123, 456])
   *   newSequence(['123', '456'],'integer')
   * </pre>
   *
   * @return an instance of {@code ScriptableValue}
   */
  public static ScriptableValue newSequence(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    Object value = args[0];
    ValueType valueType = args.length > 1 ? ValueType.Factory.forName((String) args[1]) : null;
    List<Value> values = null;
    if(value instanceof NativeArray) {
      values = nativeArrayToValueList(valueType, (NativeArray) value);
    } else {
      values = new ArrayList<>();
      values.add(valueType == null ? ValueType.Factory.newValue((Serializable) value) : valueType.valueOf(value));
    }
    if(valueType == null) {
      if(values.isEmpty()) {
        throw new IllegalArgumentException("cannot determine ValueType for null object");
      }
      valueType = values.get(0).getValueType();
    }
    return new ScriptableValue(thisObj, ValueType.Factory.newSequence(valueType, values));
  }

  private static List<Value> nativeArrayToValueList(@Nullable ValueType valueType, NativeArray nativeArray) {
    List<Value> newValues = new ArrayList<>();
    for(long i = 0; i < nativeArray.getLength(); i++) {
      Serializable serializable = (Serializable) nativeArray.get(i);
      newValues.add(valueType == null ? ValueType.Factory.newValue(serializable) : valueType.valueOf(serializable));
    }
    return newValues;
  }

  /**
   * Allows invoking {@code VariableValueSource#getValue(ValueSet)} and returns a {@code ScriptableValue}. Accessed as $
   * in javascript.
   * <p/>
   * <pre>
   *   $('Participant.firstName')
   *   $('other-collection:SMOKER_STATUS')
   * </pre>
   *
   * @return an instance of {@code ScriptableValue}
   */
  public static Scriptable $(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$() expects exactly one argument: a variable name.");
    }
    return $value(ctx, thisObj, args, funObj);
  }

  public static Scriptable $val(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$val() expects exactly one argument: a variable name.");
    }
    return $value(ctx, thisObj, args, funObj);
  }

  public static Scriptable $value(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$value() expects exactly one argument: a variable name.");
    }

    MagmaContext context = MagmaContext.asMagmaContext(ctx);

    String name = (String) args[0];

    return valueFromContext(context, thisObj, name);
  }

  /**
   * Get the value set creation timestamp.
   * <p/>
   * <pre>
   *   $created()
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   */
  public static Scriptable $created(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    MagmaContext context = MagmaContext.asMagmaContext(ctx);
    return new ScriptableValue(thisObj, timestampsFromContext(context).getCreated());
  }

  /**
   * Get the value set last update timestamp.
   * <p/>
   * <pre>
   *   $lastupdate()
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   */
  public static Scriptable $lastupdate(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    MagmaContext context = MagmaContext.asMagmaContext(ctx);
    return new ScriptableValue(thisObj, timestampsFromContext(context).getLastUpdate());
  }

  /**
   * Allows invoking {@code VariableValueSource#getValue(ValueSet)} and returns a {@code ScriptableValue}.
   * Accessed as $this in javascript. Argument is expected to be the name of a variable from the current view.
   * <p/>
   * <pre>
   *   $this('SMOKER_STATUS')
   * </pre>
   *
   * @return an instance of {@code ScriptableValue}
   */
  public static Scriptable $this(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$this() expects exactly one argument: a variable name.");
    }

    MagmaContext context = MagmaContext.asMagmaContext(ctx);

    if(!context.has(View.class)) {
      throw new IllegalArgumentException("$this() can only be used in the context of a view.");
    }

    String name = (String) args[0];

    if(name.contains(":")) {
      throw new IllegalArgumentException("$this() expects a variable name of the current view.");
    }

    return valueFromViewContext(context, thisObj, name);
  }

  /**
   * Allows joining a variable value to another variable value that provides a entity identifier. Accessed as $join in
   * javascript.
   * <p/>
   * <pre>
   *   $join('medications.Drugs:BRAND_NAME','MEDICATION_1')
   * </pre>
   *
   * @return an instance of {@code ScriptableValue}
   */
  public static Scriptable $join(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 2) {
      throw new IllegalArgumentException(
          "$join() expects exactly two arguments: the reference the variable to be joined and the name of the variable holding entity identifiers.");
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
    Value value = identifier.isSequence()
        ? joinedSource.getValueType().nullSequence()
        : joinedSource.getValueType().nullValue();
    if(identifier.isSequence()) {
      if(identifier.asSequence().getSize() > 0) {
        List<Value> joinedValues = Lists.newArrayList();
        for(Value id : identifier.asSequence().getValue()) {
          joinedValues.add(getJoinedValue(joinedTable, joinedSource, id));
        }
        value = joinedSource.getValueType().sequenceOf(joinedValues);
      }
    } else {
      value = getJoinedValue(joinedTable, joinedSource, identifier);
    }
    return new ScriptableValue(thisObj, value, joinedSource.getVariable().getUnit());
  }

  /**
   * Allows accessing the variable with the given name.
   * <p/>
   * <pre>
   * $var('DO_YOU_SMOKE')
   * </pre>
   *
   * @return an instance of {@code ScriptableVariable}
   */
  public static Scriptable $var(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    return $variable(ctx, thisObj, args, funObj);
  }

  public static Scriptable $variable(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$var() expects exactly one argument: a variable name.");
    }

    MagmaContext context = MagmaContext.asMagmaContext(ctx);
    String name = (String) args[0];

    return new ScriptableVariable(thisObj, variableFromContext(context, name));
  }

  /**
   * Allows accessing the current entity identifier.
   * <p/>
   * <pre>
   * $id()
   * </pre>
   *
   * @return an instance of {@code ScriptableValue}
   */
  public static Scriptable $id(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    return $identifier(ctx, thisObj, args, funObj);
  }

  public static Scriptable $identifier(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    MagmaContext context = MagmaContext.asMagmaContext(ctx);
    VariableEntity entity = context.peek(VariableEntity.class);
    return new ScriptableValue(thisObj, TextType.get().valueOf(entity.getIdentifier()));
  }

  /**
   * Provides 'info' level logging of messages and variables. Returns a {@code ScriptableValue}. Accessed as 'log' in
   * javascript.
   * <p/>
   * <pre>
   *   log('My message')
   *   log(onyx('org.obiba.onyx.lastExportDate'))
   *   log('The last export date: {}', onyx('org.obiba.onyx.lastExportDate'))
   *   log('The last export date: {} Days before purge: {}', onyx('org.obiba.onyx.lastExportDate'), onyx('org.obiba.onyx.participant.purge'))
   * </pre>
   *
   * @return an instance of {@code ScriptableValue}
   */
  public static Scriptable log(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length < 1) {
      throw new UnsupportedOperationException(
          "log() expects either one or more arguments. e.g. log('message'), log('var 1 {}', $('var1')), log('var 1 {} var 2 {}', $('var1'), $('var2')).");
    }
    if(args.length == 1) {
      if(args[0] instanceof Exception) {
        log.warn("Exception during JS execution", (Throwable) args[0]);
      } else {
        log.info(args[0].toString());
      }
    } else {
      log.info(args[0].toString(), Arrays.copyOfRange(args, 1, args.length));
    }
    return thisObj;
  }

  //
  // Private methods
  //

  private static Value getJoinedValue(ValueTable joinedTable, VariableValueSource joinedSource, Value identifier) {
    Value value = joinedSource.getVariable().isRepeatable()
        ? joinedSource.getValueType().nullSequence()
        : joinedSource.getValueType().nullValue();
    if(!identifier.isNull()) {
      VariableEntity entity = new VariableEntityBean(joinedTable.getEntityType(), identifier.toString());
      if(joinedTable.hasValueSet(entity)) {
        value = ensureValueNotSequence(joinedSource.getValue(joinedTable.getValueSet(entity)));
      }
    }
    return value;
  }

  /**
   * Make the value flat, in order to not have sequence of values that are value sequences.
   * @param value
   * @return
   */
  private static Value ensureValueNotSequence(Value value) {
    if (value.isSequence() && !value.asSequence().isNull()) {
      if (value.asSequence().getSize()>1) {
        value = TextType.get().valueOf(value.asSequence());
      } else {
        value = TextType.get().valueOf(value.asSequence().get(0));
      }
    }
    return value;
  }

  private static Scriptable valueFromViewContext(MagmaContext context, Scriptable thisObj, String name) {
    View view = context.peek(View.class);

    MagmaEngineVariableResolver reference = MagmaEngineVariableResolver.valueOf(name);

    // Find the named source, which is in this context a view variable value source.
    VariableValueSource source = reference.resolveSource(view);

    // Test whether this is a vector-oriented evaluation or a ValueSet-oriented evaluation
    if(context.has(VectorCache.class)) {
      return valuesForVector(context, thisObj, source);
    }
    ValueSet valueSet = context.peek(ValueSet.class);
    // The ValueSet is the one of the "from" table of the view
    ValueSet viewValueSet = view.getValueSetMappingFunction().apply(valueSet);
    Value value = source.getValue(viewValueSet);
    return new ScriptableValue(thisObj, value, source.getVariable().getUnit());
  }

  private static Timestamps timestampsFromContext(MagmaContext context) {
    // Test whether this is a vector-oriented evaluation or a ValueSet-oriented evaluation
    if(context.has(VectorCache.class)) {
      ValueTable valueTable = context.peek(ValueTable.class);
      VectorCache cache = context.peek(VectorCache.class);
      return cache.get(context, valueTable);
    } else {
      ValueSet valueSet = context.peek(ValueSet.class);
      return valueSet.getTimestamps();
    }
  }

  private static ScriptableValue valueFromContext(MagmaContext context, Scriptable thisObj, String name) {
    ValueTable valueTable = context.peek(ValueTable.class);
    MagmaEngineVariableResolver reference = MagmaEngineVariableResolver.valueOf(name);
    VariableValueSource variableSource = reference.resolveSource(valueTable);

    // Test whether this is a vector-oriented evaluation or a ValueSet-oriented evaluation
    return context.has(VectorCache.class)
        ? valuesForVector(context, thisObj, variableSource)
        : valueForValueSet(context, thisObj, reference, variableSource);
  }

  private static ScriptableValue valuesForVector(MagmaContext context, Scriptable thisObj, VariableValueSource source) {
    // Load the vector
    VectorCache cache = context.peek(VectorCache.class);
    Value value = cache.get(context, source.asVectorSource());
    return new ScriptableValue(thisObj, value, source.getVariable().getUnit());
  }

  private static ScriptableValue valueForValueSet(MagmaContext context, Scriptable thisObj,
      MagmaEngineVariableResolver reference, VariableValueSource variableSource) {
    ValueSet valueSet = context.peek(ValueSet.class);
    // Tests whether this valueSet is in the same table as the referenced ValueTable
    if(reference.isJoin(valueSet)) {
      // Resolve the joined valueSet
      try {
        valueSet = reference.join(valueSet);
      } catch(NoSuchValueSetException e) {
        // Entity does not have a ValueSet in joined collection
        // Return a null value
        return new ScriptableValue(thisObj, variableSource.getValueType().nullValue(),
            variableSource.getVariable().getUnit());
      }
    }

    Value value = variableSource.getValue(valueSet);
    return new ScriptableValue(thisObj, value, variableSource.getVariable().getUnit());
  }

  /**
   * Get occurrence group matching criteria and returns a map (variable name/{@code ScriptableValue}).
   * <p/>
   * <pre>
   *   $group('StageName','StageA')['StageDuration']
   *   $group('NumVar', function(value) {
   *     return value.ge(10);
   *   })['AnotherVar']
   *   $group('StageName','StageA', 'StageDuration')
   * </pre>
   *
   * @return a javascript object that maps variable names to {@code ScriptableValue}
   */
  public static Object $group(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
      throws MagmaJsEvaluationRuntimeException {
    if(args.length < 2 || args.length > 3) {
      throw new IllegalArgumentException(
          "$group() expects two required arguments (a variable name and a matching criteria (i.e. a value or a function)) " +
              "and one optional argument (a variable name from the same occurrence group).");
    }

    // name of the 'source' variable on which the criteria is to be applied
    String name = (String) args[0];
    // criteria for selecting values of the 'source' variable
    Object criteria = args[1];
    // 'destination' variable to be selected
    String select = args.length == 3 ? (String) args[2] : null;

    if(args.length == 2) {
      return getGroups(ctx, thisObj, name, criteria);
    }
    return new ScriptableValue(thisObj, getGroupValue(ctx, thisObj, name, criteria, select));
  }

  private static Value getGroupValue(Context ctx, Scriptable thisObj, String name, Object criteria, String select) {
    MagmaContext context = MagmaContext.asMagmaContext(ctx);
    ScriptableValue sv = valueFromContext(context, thisObj, name);
    Variable variable = variableFromContext(context, name);
    ValueTable valueTable = valueTableFromContext(context);
    Variable selectVariable = getVariableFromOccurrenceGroup(valueTable, variable, select);

    ValueSequence sourceValue = sv.getValue().asSequence();
    if(sourceValue.isNull() || !sourceValue.isSequence()) {
      return selectVariable.getValueType().nullValue();
    }

    Predicate<Value> predicate = getPredicate(ctx, sv.getParentScope(), thisObj, variable, criteria);
    ValueSequence destinationValue = valueFromContext(context, thisObj, selectVariable.getName()).getValue()
        .asSequence();

    return getSequenceGroupValue(selectVariable.getValueType(), sourceValue, predicate, destinationValue);
  }

  private static Value getSequenceGroupValue(ValueType valueType, ValueSequence sourceValue, Predicate<Value> predicate,
      ValueSequence destinationValue) {

    List<Value> rvalues = Lists.newArrayList();
    int index = -1;
    for(Value value : sourceValue.getValues()) {
      index++;
      if(predicate.apply(value) && index < destinationValue.getSize()) {
        rvalues.add(destinationValue.get(index));
      }
    }

    if(rvalues.size() == 1) {
      return rvalues.get(0);
    }
    if(rvalues.size() > 1) {
      return valueType.sequenceOf(rvalues);
    }
    return valueType.nullValue();
  }

  @Deprecated
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  private static NativeObject getGroups(Context ctx, Scriptable thisObj, String name, Object criteria) {
    MagmaContext context = MagmaContext.asMagmaContext(ctx);

    ScriptableValue sv = valueFromContext(context, thisObj, name);
    Variable variable = variableFromContext(context, name);

    NativeObject valueObject = new NativeObject();

    if(sv.getValue().isNull() || !sv.getValue().isSequence()) {
      // just map itself
      valueObject.put(variable.getName(), valueObject, sv);
    } else {
      ValueTable valueTable = valueTableFromContext(context);
      Predicate<Value> predicate = getPredicate(ctx, sv.getParentScope(), thisObj, variable, criteria);
      Iterable<Variable> variables = getVariablesFromOccurrenceGroup(valueTable, variable, null);

      ValueSequence valueSequence = sv.getValue().asSequence();
      Map<String, List<Value>> valueMap = Maps.newHashMap();
      int index = -1;
      // foreach eligible value, look for corresponding values of the same variable group
      for(Value value : valueSequence.getValue()) {
        index++;
        if(predicate.apply(value)) {
          // map itself
          addVariableValue(valueMap, variable, value);
          // get variables of the same occurrence group and map values
          mapValues(context, thisObj, valueMap, variables, index);
        }
      }

      // make it a native map
      for(Map.Entry<String, List<Value>> entry : valueMap.entrySet()) {
        List<Value> values = entry.getValue();
        Value value;
        if(values.size() == 1) {
          value = values.get(0);
        } else {
          value = values.get(0).getValueType().sequenceOf(values);
        }
        valueObject.put(entry.getKey(), valueObject, new ScriptableValue(thisObj, value));
      }
    }

    return valueObject;
  }

  @Nullable
  private static ValueTable valueTableFromContext(MagmaContext context) {
    ValueTable valueTable = null;
    if(context.has(ValueTable.class)) {
      valueTable = context.peek(ValueTable.class);
    }
    return valueTable;
  }

  private static Variable variableFromContext(MagmaContext context, String name) {
    MagmaEngineVariableResolver reference = MagmaEngineVariableResolver.valueOf(name);
    VariableValueSource source = context.has(ValueTable.class)
        ? reference.resolveSource(context.peek(ValueTable.class))
        : reference.resolveSource();
    return source.getVariable();
  }

  private static Predicate<Value> getPredicate(Context ctx, Scriptable scope, Scriptable thisObj, Variable variable,
      Object criteria) {
    Predicate<Value> predicate;
    if(criteria instanceof ScriptableValue) {
      predicate = new ValuePredicate(((ScriptableValue) criteria).getValue());
    } else if(criteria instanceof Function) {
      predicate = new FunctionPredicate(ctx, scope, thisObj, (Function) criteria);
    } else {
      predicate = new ValuePredicate(variable.getValueType().valueOf(criteria));
    }
    return predicate;
  }

  private static Variable getVariableFromOccurrenceGroup(@Nullable ValueTable valueTable, @NotNull Variable variable,
      @NotNull String select) {
    List<Variable> variables = getVariablesFromOccurrenceGroup(valueTable, variable, select);
    if(variables.size() != 1) {
      throw new IllegalArgumentException(
          "Cannot find one variable with name '" + select + "' in the same occurrence group as '" + variable.getName() +
              "'");
    }

    return variables.get(0);
  }

  private static List<Variable> getVariablesFromOccurrenceGroup(@Nullable ValueTable valueTable,
      @NotNull Variable variable, String select) {
    ImmutableList.Builder<Variable> builder = ImmutableList.builder();

    if(variable.getOccurrenceGroup() == null || valueTable == null) {
      return builder.build();
    }

    for(Variable var : valueTable.getVariables()) {
      if(variable.getOccurrenceGroup().equals(var.getOccurrenceGroup())) {
        if(select == null || select.equals(var.getName())) {
          builder.add(var);
        }
      }
    }

    return builder.build();
  }

  private static void addVariableValue(Map<String, List<Value>> valueMap, Variable variable, Value value) {
    List<Value> values = valueMap.get(variable.getName());
    if(values == null) {
      values = Lists.newArrayList();
      valueMap.put(variable.getName(), values);
    }
    values.add(value);
  }

  private static void mapValues(MagmaContext context, Scriptable thisObj, Map<String, List<Value>> valueMap,
      Iterable<Variable> variables, int index) {
    if(index < 0) return;

    for(Variable var : variables) {
      ScriptableValue scriptableValue = valueFromContext(context, thisObj, var.getName());
      Value value = var.getValueType().nullValue();
      if(!scriptableValue.getValue().isNull()) {
        ValueSequence valSeq = scriptableValue.getValue().asSequence();
        if(index < valSeq.getSize()) {
          value = valSeq.get(index);
        }
      }
      addVariableValue(valueMap, var, value);
    }
  }

  /**
   * Predicate based on a function call.
   */
  private static final class FunctionPredicate implements Predicate<Value> {

    private final Context ctx;

    private final Scriptable scope;

    private final Scriptable thisObj;

    private final Function criteriaFunction;

    private FunctionPredicate(Context ctx, Scriptable scope, Scriptable thisObj, Function criteriaFunction) {
      this.ctx = ctx;
      this.scope = scope;
      this.thisObj = thisObj;
      this.criteriaFunction = criteriaFunction;
    }

    @Override
    public boolean apply(@Nullable Value input) {
      if(input == null) return false;
      Object rval = criteriaFunction
          .call(ctx, scope, thisObj, new ScriptableValue[] { new ScriptableValue(thisObj, input) });
      if(rval instanceof ScriptableValue) {
        Value value = ((ScriptableValue) rval).getValue();
        if(value.isNull()) return false;
        rval = value.getValue();
      }
      return rval == null ? false : (Boolean) rval;
    }
  }

  /**
   * Predicate based on the equality with a value.
   */
  private static final class ValuePredicate implements Predicate<Value> {

    @NotNull
    private final Value criteriaValue;

    private ValuePredicate(@NotNull Value criteriaValue) {
      this.criteriaValue = criteriaValue;
    }

    @Override
    public boolean apply(@Nullable Value input) {
      return Objects.equal(input, criteriaValue);
    }
  }
}
