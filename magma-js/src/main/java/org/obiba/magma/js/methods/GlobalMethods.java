package org.obiba.magma.js.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
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
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  public Set<String> getExposedMethods() {
    return GLOBAL_METHODS;
  }

  /**
   * Creates an instance of {@code ScriptableValue} containing the current date and time.
   *
   * @return an instance of {@code ScriptableValue} containing the current date and time.
   */
  public static ScriptableValue now(MagmaContext cx, Object[] args) {
    return new ScriptableValue(DateTimeType.get().valueOf(new Date()));
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
  public static ScriptableValue newValue(MagmaContext cx, Object[] args) {
    Object value = ensurePrimitiveValue(args[0]);
    Value v = args.length > 1
        ? ValueType.Factory.forName((String) args[1]).valueOf(value)
        : ValueType.Factory.newValue((Serializable) value);

    return new ScriptableValue(v);
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
  public static ScriptableValue newSequence(MagmaContext cx, Object[] args) {
    Object value = args[0];
    ValueType valueType = args.length > 1 ? ValueType.Factory.forName((String) args[1]) : null;
    List<Value> values;

    if(value instanceof ScriptObjectMirror && ((ScriptObjectMirror)value).isArray()) {
      values = nativeArrayToValueList(valueType, (ScriptObjectMirror)value);
    } else {
      values = new ArrayList<>();
      value = ensurePrimitiveValue(value);
      values.add(valueType == null ? ValueType.Factory.newValue((Serializable) value) : valueType.valueOf(value));
    }

    if(valueType == null) {
      if(values.isEmpty()) {
        throw new IllegalArgumentException("cannot determine ValueType for null object");
      }
      valueType = values.get(0).getValueType();
    }

    return new ScriptableValue(ValueType.Factory.newSequence(valueType, values));
  }

  private static List<Value> nativeArrayToValueList(@Nullable ValueType valueType, ScriptObjectMirror nativeArray) {
    List<Value> newValues = new ArrayList<>();

    nativeArray.keySet().forEach(k -> {
      Object value = ensurePrimitiveValue(nativeArray.get(k));
      Serializable serializable = (Serializable) value;
      newValues.add(valueType == null ? ValueType.Factory.newValue(serializable) : valueType.valueOf(serializable));
    });

    return newValues;
  }

  private static Object ensurePrimitiveValue(Object value) {
    Object rvalue = value;

    if (value instanceof ScriptableValue) {
      rvalue = ((ScriptableValue) value).getValue().getValue();
    } else if (value instanceof String) {
      rvalue = value.toString();
    }

    return rvalue;
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
  public static ScriptableValue $(MagmaContext ctx, Object[] args) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$() expects exactly one argument: a variable name.");
    }

    return $value(ctx, args);
  }

  public static ScriptableValue $val(MagmaContext ctx, Object[] args) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$val() expects exactly one argument: a variable name.");
    }

    return $value(ctx, args);
  }

  public static ScriptableValue $value(MagmaContext ctx, Object[] args) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$value() expects exactly one argument: a variable name.");
    }

    String name = (String) args[0];

    return valueFromContext(ctx, name);
  }

  /**
   * Get the value set creation timestamp.
   * <p/>
   * <pre>
   *   $created()
   * </pre>
   *
   * @param ctx
   * @param args
   * @return
   */
  public static ScriptableValue $created(MagmaContext ctx, Object[] args) {
    return new ScriptableValue(timestampsFromContext(ctx).getCreated());
  }

  /**
   * Get the value set last update timestamp.
   * <p/>
   * <pre>
   *   $lastupdate()
   * </pre>
   *
   * @param ctx
   * @param args
   * @return
   */
  public static ScriptableValue $lastupdate(MagmaContext ctx,  Object[] args) {
    return new ScriptableValue(timestampsFromContext(ctx).getLastUpdate());
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
  public static ScriptableValue $this(MagmaContext ctx, Object[] args) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$this() expects exactly one argument: a variable name.");
    }

    if(ctx.get(View.class) == null) {
      throw new IllegalArgumentException("$this() can only be used in the context of a view.");
    }

    String name = (String) args[0];

    if(name.contains(":")) {
      throw new IllegalArgumentException("$this() expects a variable name of the current view.");
    }

    try {
      return valueFromViewContext(ctx, name);
    } catch(Exception e) {
      return valueFromViewContext(ctx, name);
    }
  }

  /**
   * Allows joining a variable value to another variable value that provides a entity identifier. Accessed as $join in
   * javascript. The treament of value sequences of value sequences is optional:
   * <ul>
   *   <li>keep the occurrence order and therefore the values of a sequence are turned into a csv string,</li>
   *   <li>flatten the value sequence tree into a squence of unique values.</li>
   * </ul>
   * <p/>
   * <pre>
   *   $join('medications.Drugs:BRAND_NAME','MEDICATION_1')
   *   $join('test.tbl:SEQ','VARSEQ', true)
   * </pre>
   *
   * @return an instance of {@code ScriptableValue}
   */
  public static ScriptableValue $join(MagmaContext ctx, Object[] args) {
    if(args.length < 2) {
      throw new IllegalArgumentException(
          "$join() expects exactly two arguments: the reference the variable to be joined and the name of the variable holding entity identifiers.");
    }

    String joinedName = (String) args[0];
    String name = (String) args[1];
    boolean flat = false;
    if (args.length == 3) {
      try {
        flat = (Boolean) BooleanType.get().valueOf(args[2]).getValue();
      } catch (Exception ignore) {}
    }

    ValueTable valueTable = (ValueTable)(ctx.get(ValueTable.class));
    Value identifier = valueFromContext(ctx, name).getValue();

    // Find the joined named source
    MagmaEngineVariableResolver reference = MagmaEngineVariableResolver.valueOf(joinedName);
    ValueTable joinedTable = reference.resolveTable(valueTable);
    VariableValueSource joinedSource = reference.resolveSource(valueTable);

    return new ScriptableValue(getJoinedValue(joinedTable, joinedSource, identifier, flat),
        joinedSource.getVariable().getUnit());
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
  public static ScriptableVariable $var(MagmaContext ctx, Object[] args) {
    return $variable(ctx, args);
  }

  public static ScriptableVariable $variable(MagmaContext ctx, Object[] args) {
    if(args.length != 1) {
      throw new IllegalArgumentException("$var() expects exactly one argument: a variable name.");
    }

    String name = (String) args[0];

    return new ScriptableVariable(variableFromContext(ctx, name));
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
  public static ScriptableValue $id(MagmaContext ctx, Object[] args) {
    return $identifier(ctx, args);
  }

  public static ScriptableValue $identifier(MagmaContext ctx, Object[] args) {
    VariableEntity entity = (VariableEntity) (ctx).get(VariableEntity.class);

    return new ScriptableValue(TextType.get().valueOf(entity.getIdentifier()));
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
  public static ScriptableValue log(MagmaContext ctx, Object[] args) {
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

    return args.length > 1 ? (ScriptableValue) args[args.length - 1] : null;
  }

  //
  // Private methods
  //

  /**
   * Get a joined value where identifier can be a sequence of identifiers.
   *
   * @param joinedTable
   * @param joinedSource
   * @param identifier
   * @param flat Flatten the value sequence tree into a sequence of unique values
   * @return
   */
  private static Value getJoinedValue(ValueTable joinedTable, VariableValueSource joinedSource, Value identifier, boolean flat) {
    // Default value is null if joined table has no valueSet (equivalent to a LEFT JOIN)
    Value value = identifier.isSequence()
        ? joinedSource.getValueType().nullSequence()
        : joinedSource.getValueType().nullValue();
    if(identifier.isSequence()) {
      if(identifier.asSequence().getSize() > 0) {
        List<Value> joinedValues = Lists.newArrayList();
        for(Value id : identifier.asSequence().getValue()) {
          joinedValues.add(getSingleJoinedValue(joinedTable, joinedSource, id, flat));
        }
        value = joinedSource.getValueType().sequenceOf(joinedValues);
        if (flat) {
          value = value.getValueType().sequenceOf(new HashSet<>(getAllSingleValues(value.asSequence())));
        }
      }
    } else {
      value = getSingleJoinedValue(joinedTable, joinedSource, identifier, true);
    }

    return value;
  }

  /**
   * Get a joined value where identifier must not be a sequence of identifiers.
   *
   * @param joinedTable
   * @param joinedSource
   * @param identifier
   * @param allowSequence
   * @return
   */
  private static Value getSingleJoinedValue(ValueTable joinedTable, VariableValueSource joinedSource,
      Value identifier, boolean allowSequence) {
    Value value = identifier.isSequence()
        ? joinedSource.getValueType().nullSequence()
        : joinedSource.getValueType().nullValue();
    if(!identifier.isNull()) {
      VariableEntity entity = new VariableEntityBean(joinedTable.getEntityType(), identifier.toString());
      if(joinedTable.hasValueSet(entity)) {
        value = joinedSource.getValue(joinedTable.getValueSet(entity));
        value = allowSequence ? value : ensureValueNotSequence(value);
      }
    }
    return value;
  }

  /**
   * Make the value flat, in order to not have sequence of values that are value sequences.
   *
   * @param value
   * @return
   */
  private static Value ensureValueNotSequence(Value value) {
    Value rval = value;
    if(value.isSequence() && !value.asSequence().isNull()) {
      if(value.asSequence().getSize() > 1) {
        rval = TextType.get().valueOf(value.asSequence());
      } else {
        rval = TextType.get().valueOf(value.asSequence().get(0));
      }
    }
    return rval;
  }

  /**
   * Recursively gets all the values in the given sequence
   * @param seq
   * @return list of values
   */
  public static List<Value> getAllSingleValues(ValueSequence seq) {
    List<Value> list = new ArrayList<>();
    extractSingleValues(seq, list);
    return list;
  }

  private static void extractSingleValues(ValueSequence seq, List<Value> toAdd) {
    for (Value v: seq.getValues()) {
      if (v.isSequence()) {
        extractSingleValues((ValueSequence)v, toAdd);
      } else {
        toAdd.add(v);
      }
    }
  }

  private static ScriptableValue valueFromViewContext(MagmaContext context, String name) {
    View view = (View)context.get(View.class);

    MagmaEngineVariableResolver reference = MagmaEngineVariableResolver.valueOf(name);

    // Find the named source, which is in this context a view variable value source.
    VariableValueSource source = reference.resolveSource(view);

    // Test whether this is a vector-oriented evaluation or a ValueSet-oriented evaluation
    if(context.get(VectorCache.class) != null) {
      return valuesForVector(context, source);
    }

    ValueSet valueSet = (ValueSet) context.get(ValueSet.class);
    //The ValueSet is the one of the "from" table of the view
    ValueSet viewValueSet = view.getValueSetMappingFunction().apply(valueSet);
    Value value = source.getValue(viewValueSet);

    return new ScriptableValue(value, source.getVariable().getUnit());
  }

  private static Timestamps timestampsFromContext(MagmaContext context) {
    // Test whether this is a vector-oriented evaluation or a ValueSet-oriented evaluation
    if(context.get(VectorCache.class) != null) {
      ValueTable valueTable = (ValueTable) context.get(ValueTable.class);
      VectorCache cache = (VectorCache) context.get(VectorCache.class);

      return cache.get(context, valueTable);
    } else {
      ValueSet valueSet = (ValueSet) context.get(ValueSet.class);
      return valueSet.getTimestamps();
    }
  }

  private static ScriptableValue valueFromContext(MagmaContext context, String name) {
    ValueTable valueTable = (ValueTable)context.get(ValueTable.class);
    MagmaEngineVariableResolver reference = MagmaEngineVariableResolver.valueOf(name);
    VariableValueSource variableSource = reference.resolveSource(valueTable);

    // Test whether this is a vector-oriented evaluation or a ValueSet-oriented evaluation
    return context.get(VectorCache.class) != null
        ? valuesForVector(context,  variableSource)
        : valueForValueSet(context, reference, variableSource);
  }

  private static ScriptableValue valuesForVector(MagmaContext context, VariableValueSource source) {
    // Load the vector
    VectorCache cache = (VectorCache)context.get(VectorCache.class);
    Value value = cache.get(context, source.asVectorSource());
    return new ScriptableValue(value, source.getVariable().getUnit());
  }

  private static ScriptableValue valueForValueSet(MagmaContext context,
      MagmaEngineVariableResolver reference, VariableValueSource variableSource) {
    ValueSet valueSet = (ValueSet)(context).get(ValueSet.class);
    // Tests whether this valueSet is in the same table as the referenced ValueTable
    if(reference.isJoin(valueSet)) {
      // Resolve the joined valueSet
      try {
        valueSet = reference.join(valueSet);
      } catch(NoSuchValueSetException e) {
        // Entity does not have a ValueSet in joined collection
        // Return a null value
        return new ScriptableValue(variableSource.getValueType().nullValue(), variableSource.getVariable().getUnit());
      }
    }

    Value value = variableSource.getValue(valueSet);
    return new ScriptableValue(value, variableSource.getVariable().getUnit());
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
  /*public static Object $group(MagmaContext ctx, ScriptableValue thisObj, Object[] args)
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

    return new ScriptableValue(getGroupValue(ctx, thisObj, name, criteria, select));
  }*/

  /*private static Value getGroupValue(MagmaContext ctx, ScriptableValue thisObj, String name, Object criteria, String select) {
    ScriptableValue sv = valueFromContext(ctx, thisObj, name);
    Variable variable = variableFromContext(ctx, name);
    ValueTable valueTable = valueTableFromContext(ctx);
    Variable selectVariable = getVariableFromOccurrenceGroup(valueTable, variable, select);

    ValueSequence sourceValue = sv.getValue().asSequence();
    if(sourceValue.isNull() || !sourceValue.isSequence()) {
      return selectVariable.getValueType().nullValue();
    }

    Predicate<Value> predicate = getPredicate(ctx, sv.getParentScope(), thisObj, variable, criteria);
    ValueSequence destinationValue = valueFromContext(ctx, thisObj, selectVariable.getName()).getValue()
        .asSequence();

    return getSequenceGroupValue(selectVariable.getValueType(), sourceValue, predicate, destinationValue);
  }*/

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

  /*
  @Deprecated
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  private static Object getGroups(MagmaContext ctx, ScriptableValue thisObj, String name, Object criteria) {
    ScriptableValue sv = valueFromContext(ctx, thisObj, name);
    Variable variable = variableFromContext(ctx, name);

    Object valueObject = new Object();

    if(sv.getValue().isNull() || !sv.getValue().isSequence()) {
      // just map itself
      valueObject.put(variable.getName(), valueObject, sv);
    } else {
      ValueTable valueTable = valueTableFromContext(ctx);
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
          mapValues(ctx, thisObj, valueMap, variables, index);
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
        valueObject.put(entry.getKey(), valueObject, new ScriptableValue(value));
      }
    }

    return valueObject;
  }*/

  @Nullable
  private static ValueTable valueTableFromContext(MagmaContext context) {
    ValueTable valueTable = null;
    if(context.get(ValueTable.class) != null) {
      valueTable = (ValueTable) context.get(ValueTable.class);
    }

    return valueTable;
  }

  private static Variable variableFromContext(MagmaContext context, String name) {
    MagmaEngineVariableResolver reference = MagmaEngineVariableResolver.valueOf(name);
    VariableValueSource source = context.get(ValueTable.class) != null
        ? reference.resolveSource((ValueTable)context.get(ValueTable.class))
        : reference.resolveSource();

    return source.getVariable();
  }

  /*private static Predicate<Value> getPredicate(MagmaContext ctx, ScriptableValue scope, ScriptableValue thisObj, Variable variable,
      Object criteria) {
    Predicate<Value> predicate;
    if(criteria instanceof ScriptableValue) {
      predicate = new ValuePredicate(((ScriptableValue) criteria).getValue());
    } else if(criteria instanceof Function) {
      predicate = new FunctionPredicate(ctx, scope, thisObj, criteria);
    } else {
      predicate = new ValuePredicate(variable.getValueType().valueOf(criteria));
    }
    return predicate;
  }*/

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

  private static void mapValues(MagmaContext context, ScriptableValue thisObj, Map<String, List<Value>> valueMap,
      Iterable<Variable> variables, int index) {
    if(index < 0) return;

    for(Variable var : variables) {
      ScriptableValue scriptableValue = valueFromContext(context, var.getName());
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
  /*private static final class FunctionPredicate implements Predicate<Value> {

    private final Context ctx;

    private final Scriptable scope;

    private final ScriptableValue thisObj;

    private final ScriptObjectMirror criteriaFunction;

    private FunctionPredicate(MagmaContext ctx, ScriptableValue scope, ScriptableValue thisObj, ScriptObjectMirror criteriaFunction) {
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
  }*/

  /**
   * Predicate based on the equality with a value.
   */
  /*private static final class ValuePredicate implements Predicate<Value> {

    @NotNull
    private final Value criteriaValue;

    private ValuePredicate(@NotNull Value criteriaValue) {
      this.criteriaValue = criteriaValue;
    }

    @Override
    public boolean apply(@Nullable Value input) {
      return Objects.equal(input, criteriaValue);
    }
  }*/
}
