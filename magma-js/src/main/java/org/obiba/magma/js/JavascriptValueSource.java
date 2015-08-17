package org.obiba.magma.js;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.validation.constraints.NotNull;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
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

import static com.google.common.base.Throwables.propagate;

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
  private transient CompiledScript compiledScript;

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
    MagmaContext context = MagmaContextFactory.createContext();
    context.setAttribute(ScriptEngine.FILENAME, getScriptName(), ScriptContext.ENGINE_SCOPE);
    //context.setBindings(new SimpleBindings(), ScriptContext.ENGINE_SCOPE);
    Map<Object, Object> shared = getShared();
    shared.put(ValueSet.class, valueSet);
    shared.put(ValueTable.class, valueSet.getValueTable());
    shared.put(VariableEntity.class, valueSet.getVariableEntity());

    Value value = context.exec(() -> {
      try {
        return asValue(compiledScript.eval(context));
      } catch (ScriptException e) {
        throw Throwables.propagate(e);
      }
    }, shared);

    return value;
  }

  protected Map<Object, Object> getShared() {
    return Maps.newHashMap();
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
    Map<Object, Object> shared = getShared();
    shared.put(SortedSet.class, entities);
    final VectorCache vectorCache = new VectorCache();
    shared.put(VectorCache.class, vectorCache);
    MagmaContext context = MagmaContextFactory.createContext();

    return context.exec(() -> {
      Iterable<Value> values = Iterables.transform(entities, new VectorEvaluationFunction(context, vectorCache));
      return Lists.newArrayList(values);
    }, shared);
  }

  private class VectorEvaluationFunction implements Function<VariableEntity, Value> {
    private final MagmaContext context;
    private final VectorCache vectorCache;

    private VectorEvaluationFunction(MagmaContext context, VectorCache vectorCache) {
      this.context = context;
      this.vectorCache = vectorCache;
    }

    @Override
    public Value apply(VariableEntity variableEntity) {
      try {
        context.push(VariableEntity.class, variableEntity);
        return asValue(compiledScript.eval(context));
      } catch(ScriptException e) {
        throw propagate(e);
      } finally {
        context.pop(VariableEntity.class);
        vectorCache.next();
      }
    }
  }

  @NotNull
  @Override
  public ValueType getValueType() {
    return type;
  }

  @Override
  public void initialise() {
  }

  protected synchronized void initialiseIfNot() {
    if(compiledScript == null) {
      try {
        compiledScript = ((Compilable) MagmaContextFactory.getEngine()).compile(getScript());
      } catch (Exception e) {
        log.error("Script compilation failed: {}", getScript(), e);

        if (e instanceof ScriptException) {
          ScriptException se = (ScriptException) e;
          e = new ScriptException(se.getMessage(), getScriptName(), se.getLineNumber(), se.getColumnNumber());
        }

        throw new MagmaJsRuntimeException("Script compilation failed: " + e.getMessage(), e);
      }
    }
  }

  Value asValue(Object value) {
    Value result;

    if(value == null || (value instanceof ScriptObjectMirror && ScriptObjectMirror.isUndefined(value))) {
      result = isSequence() ? getValueType().nullSequence() : getValueType().nullValue();
    } else if(value instanceof ScriptableValue) {
      ScriptableValue scriptableValue = (ScriptableValue) value;

      if(scriptableValue.getValue().isSequence() != isSequence()) {
        throw new MagmaJsRuntimeException(
            "The returned value is " + (isSequence() ? "" : "not ") + "expected to be a value sequence.");
      }
      result = scriptableValue.getValue();
    } else if(value instanceof ScriptObjectMirror) {
      value = ((ScriptObjectMirror)value).getSlot(0);

      if(ScriptObjectMirror.isUndefined(value)) {
        result = isSequence() ? getValueType().nullSequence() : getValueType().nullValue();
      } else {
        result = isSequence() ? asValueSequence(value) : getValueType().valueOf(value);
      }
    } else {
      result = isSequence() ? asValueSequence(value) : getValueType().valueOf(value);
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
    Value result;

    if(value.getClass().isArray()) {
      int length = Array.getLength(value);
      Collection<Value> values = new ArrayList<>(length);

      for(int i = 0; i < length; i++) {
        Object v = Array.get(value, i);
        values.add(getValueType().valueOf(v));
      }

      result = getValueType().sequenceOf(values);
    } else {
      // Build a singleton sequence
      result = getValueType().sequenceOf(ImmutableList.of(getValueType().valueOf(value)));
    }

    return result;
  }

  protected boolean isSequence() {
    return false;
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
    public Value get(ScriptContext context, VectorSource source) {
      VectorHolder<Value> holder = vectors.get(source);

      if(holder == null) {
        holder = new VectorHolder<>(
            source.getValues((SortedSet<VariableEntity>) ((MagmaContext)context).get(SortedSet.class)).iterator());
        vectors.put(source, holder);
      }

      return holder.get(index);
    }

    public Timestamps get(ScriptContext context, ValueTable table) {
      if(timestampsVector == null) {
        timestampsVector = new VectorHolder<>(
            table.getValueSetTimestamps((SortedSet<VariableEntity>) ((MagmaContext)context).get(SortedSet.class))
                .iterator());
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
        if(!values.hasNext()) break;
        currentValue = values.next();
        nextIndex++;
      }

      return currentValue;
    }
  }
}
