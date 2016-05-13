package org.obiba.magma.js;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.obiba.magma.Initialisable;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VectorSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * A {@code ValueSource} implementation that uses a JavaScript script to evaluate the {@code Value} to return.
 * <p/>
 * Within the JavaScript engine, {@code Value} instances are represented by {@code ScriptableValue} host objects.
 * <p/>
 * This class implements {@code Initialisable}. During the {@code #initialise()} method, the provided script is
 * compiled. Any compile error is thrown as a {@code EvaluatorException} which contains the details of the error.
 *
 * @see ScriptableValue
 */
public class JavascriptValueSource implements ValueSource, VectorSource, Initialisable {

  private static final Logger log = LoggerFactory.getLogger(JavascriptValueSource.class);

  @NotNull
  private final ValueType type;

  @NotNull
  private final String script;

  private String scriptName = "customScript";

  // need to be transient because of XML serialization
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient Script compiledScript;

  @SuppressWarnings("ConstantConditions")
  public JavascriptValueSource(@NotNull ValueType type, @NotNull String script) {
    if(type == null) throw new IllegalArgumentException("type cannot be null");
    if(script == null) throw new IllegalArgumentException("script cannot be null");
    this.type = type;
    this.script = script;
  }

  public String getScriptName() {
    return scriptName;
  }

  public void setScriptName(String name) {
    scriptName = name;
  }

  @NotNull
  public String getScript() {
    return script;
  }

  @NotNull
  @Override
  public Value getValue(ValueSet valueSet) {
    initialiseIfNot();
    Stopwatch stopwatch = Stopwatch.createStarted();
    Value value = (Value) ContextFactory.getGlobal().call(new ValueSetEvaluationContextAction(valueSet));
    log.trace("ValueSet evaluation of {} in {}", getScriptName(), stopwatch);
    return value;
  }

  @Override
  public boolean supportVectorSource() {
    return true;
  }

  @NotNull
  @Override
  public VectorSource asVectorSource() {
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
    initialiseIfNot();
    Stopwatch stopwatch = Stopwatch.createStarted();
    Iterable<Value> values = (Iterable<Value>) ContextFactory.getGlobal()
        .call(new ValueVectorEvaluationContextAction(entities));
    log.trace("Vector evaluation of {} in {}", getScriptName(), stopwatch);
    return values;
  }

  @NotNull
  @Override
  public ValueType getValueType() {
    return type;
  }

  @Override
  public void initialise() {
  }

  protected void initialiseIfNot() {
    if(compiledScript == null) {
      try {
        compiledScript = (Script) ContextFactory.getGlobal().call(new ContextAction() {
          @Override
          public Object run(Context context) {
            String optLevel = System.getProperty("rhino.opt.level");
            if (optLevel != null) {
              try {
                context.setOptimizationLevel(Integer.parseInt(optLevel));
              } catch(Exception e) {}
            }
            return context.compileString(getScript(), getScriptName(), 1, null);
          }
        });
      } catch(Exception e) {
        log.error("Script compilation failed: {}", getScript(), e);
        throw new MagmaJsRuntimeException("Script compilation failed: " + e.getMessage(), e);
      }
    }
  }

  protected boolean isSequence() {
    return false;
  }

  /**
   * This method is invoked before evaluating the script. It provides a chance for derived classes to initialise values
   * within the context. This method will add the current {@code ValueSet} as a {@code ThreadLocal} variable with
   * {@code ValueSet#class} as its key. This allows other classes to have access to the current {@code ValueSet} during
   * the script's execution.
   * <p/>
   * Classes overriding this method must call their super class' method
   *
   * @param ctx the current context
   * @param scope the scope of execution of this script
   * @param valueSet the current {@code ValueSet}
   */
  protected void enterContext(MagmaContext ctx, Scriptable scope) {
  }

  protected void exitContext(MagmaContext ctx) {
  }

  private abstract class AbstractEvaluationContextAction implements ContextAction {

    @Override
    public Object run(Context ctx) {
      MagmaContext context = MagmaContext.asMagmaContext(ctx);
      // Don't pollute the global scope
      Scriptable scope = context.newLocalScope();

      enterContext(context, scope);
      try {
        return eval(context, scope);
      } finally {
        exitContext(context);
      }
    }

    void enterContext(MagmaContext context, Scriptable scope) {
      JavascriptValueSource.this.enterContext(context, scope);
    }

    void exitContext(MagmaContext context) {
      JavascriptValueSource.this.exitContext(context);
    }

    abstract Object eval(MagmaContext context, Scriptable scope);

    Value asValue(Object value) {
      Value result = null;
      if(value == null || value instanceof Undefined) {
        result = isSequence() ? getValueType().nullSequence() : getValueType().nullValue();
      } else if(value instanceof ScriptableValue) {
        ScriptableValue scriptableValue = (ScriptableValue) value;
        if(scriptableValue.getValue().isSequence() != isSequence()) {
          throw new MagmaJsRuntimeException(
              "The returned value is " + (isSequence() ? "" : "not ") + "expected to be a value sequence.");
        }
        result = scriptableValue.getValue();
      } else {
        result = isSequence() ? asValueSequence(value) : getValueType().valueOf(Rhino.fixRhinoNumber(value));
      }

      if(result.getValueType() != getValueType()) {
        // Convert types
        try {
          result = getValueType().convert(result);
        } catch(RuntimeException e) {
          throw new MagmaJsRuntimeException(
              "Cannot convert value '" + result + "' to type '" + getValueType().getName() + "'", e);
        }
      }
      return result;
    }

    Value asValueSequence(Object value) {
      Value result = null;
      if(value.getClass().isArray()) {
        int length = Array.getLength(value);
        Collection<Value> values = new ArrayList<>(length);
        for(int i = 0; i < length; i++) {
          Object v = Rhino.fixRhinoNumber(Array.get(value, i));
          values.add(getValueType().valueOf(v));
        }
        result = getValueType().sequenceOf(values);
      } else {
        // Build a singleton sequence
        result = getValueType().sequenceOf(ImmutableList.of(getValueType().valueOf(Rhino.fixRhinoNumber(value))));
      }
      return result;
    }
  }

  private final class ValueSetEvaluationContextAction extends AbstractEvaluationContextAction {

    private final ValueSet valueSet;

    ValueSetEvaluationContextAction(ValueSet valueSet) {
      this.valueSet = valueSet;
    }

    @Override
    void enterContext(MagmaContext context, Scriptable scope) {
      context.push(ValueSet.class, valueSet);
      context.push(ValueTable.class, valueSet.getValueTable());
      context.push(VariableEntity.class, valueSet.getVariableEntity());
      super.enterContext(context, scope);
    }

    @Override
    void exitContext(MagmaContext context) {
      context.pop(VariableEntity.class);
      context.pop(ValueTable.class);
      context.pop(ValueSet.class);
      super.exitContext(context);
    }

    @Override
    Object eval(MagmaContext context, Scriptable scope) {
      return asValue(compiledScript.exec(context, scope));
    }

  }

  private final class ValueVectorEvaluationContextAction extends AbstractEvaluationContextAction {

    @Nullable
    private final SortedSet<VariableEntity> entities;

    private final VectorCache vectorCache = new VectorCache();

    ValueVectorEvaluationContextAction(@Nullable SortedSet<VariableEntity> entities) {
      this.entities = entities;
    }

    SortedSet<VariableEntity> getEntities(MagmaContext context) {
      return entities == null ? new TreeSet<>(context.peek(ValueTable.class).getVariableEntities()) : entities;
    }

    @Override
    void enterContext(MagmaContext context, Scriptable scope) {
      super.enterContext(context, scope);
      context.push(SortedSet.class, getEntities(context));
      context.push(VectorCache.class, vectorCache);
    }

    @Override
    void exitContext(MagmaContext context) {
      super.exitContext(context);
      context.pop(SortedSet.class);
      context.pop(VectorCache.class);
    }

    @Override
    Object eval(MagmaContext context, Scriptable scope) {
      return Iterables.transform(getEntities(context), new VectorEvaluationFunction(context, scope));
    }

    private class VectorEvaluationFunction implements Function<VariableEntity, Value> {

      private final MagmaContext context;

      private final Scriptable scope;

      private VectorEvaluationFunction(MagmaContext context, Scriptable scope) {
        this.context = context;
        this.scope = scope;
      }

      @Override
      public Value apply(VariableEntity variableEntity) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
          initContext(variableEntity);
          return asValue(compiledScript.exec(context, scope));
        } finally {
          cleanContext();
          log.trace("Finish {} eval in {}", variableEntity, stopwatch);
        }
      }

      /**
       * We have to set the current thread's context because this code will be executed outside of the ContextAction
       */
      private void initContext(VariableEntity variableEntity) {
        ContextFactory.getGlobal().enterContext(context);
        JavascriptValueSource.this.enterContext(context, scope);
        context.push(VectorCache.class, vectorCache);
        context.push(SortedSet.class, entities);
        context.push(VariableEntity.class, variableEntity);
      }

      private void cleanContext() {
        JavascriptValueSource.this.exitContext(context);
        context.pop(VectorCache.class).next();
        context.pop(SortedSet.class);
        context.pop(VariableEntity.class);
        Context.exit();
      }
    }

  }

  public static class VectorCache {

    private final Map<VectorSource, VectorHolder<Value>> vectors = Maps.newHashMap();

    private VectorHolder<Timestamps> timestampsVector;

    // Holds the current "row" of the evaluation.
    private int index = 0;

    void next() {
      index++;
    }

    // Returns the value of the current "row" for the specified vector
    @SuppressWarnings("unchecked")
    public Value get(MagmaContext context, VectorSource source) {
      VectorHolder<Value> holder = vectors.get(source);
      if(holder == null) {
        holder = new VectorHolder<>(source.getValues(context.peek(SortedSet.class)).iterator());
        vectors.put(source, holder);
      }
      return holder.get(index);
    }

    public Timestamps get(MagmaContext context, ValueTable table) {
      if (timestampsVector == null) {
        timestampsVector = new VectorHolder<>(table.getValueSetTimestamps(context.peek(SortedSet.class)).iterator());
      }
      return timestampsVector.get(index);
    }
  }

  private static class VectorHolder<T> {

    private final Iterator<T> values;

    // The index of the value returned by values.next();
    private int nextIndex = 0;

    // Value of nextIndex - 1 (null after vector)
    private T currentValue;

    VectorHolder(Iterator<T> values) {
      this.values = values;
    }

    /**
     * Returns the value of the "row" for this vector. This method will advance the iterator until we reach the
     * requested row. This is required because during evaluation, not all vectors involved in a script are incremented
     * during evaluation (due to 'if' statements in the script).
     * <p/>
     * For example, in the following script:
     * <p/>
     * <pre>
     * $('VAR1') ? $('VAR2') : $('VAR3')
     *
     * <pre>
     * vectors for VAR2 and VAR3 are not incremented at the same "rate" as VAR1.
     */
    T get(int index) {
      if(index < 0) throw new IllegalArgumentException("index must be >= 0");
      // Increment the iterator until we reach the requested row
      while(nextIndex <= index) {
        currentValue = values.next();
        nextIndex++;
      }
      return currentValue;
    }
  }
}
