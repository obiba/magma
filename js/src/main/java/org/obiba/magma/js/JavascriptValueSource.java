package org.obiba.magma.js;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.obiba.magma.Initialisable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VectorSource;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * A {@code ValueSource} implementation that uses a JavaScript script to evaluate the {@code Value} to return.
 * <p>
 * Within the JavaScript engine, {@code Value} instances are represented by {@code ScriptableValue} host objects.
 * <p>
 * This class implements {@code Initialisable}. During the {@code #initialise()} method, the provided script is
 * compiled. Any compile error is thrown as a {@code EvaluatorException} which contains the details of the error.
 * 
 * @see ScriptableValue
 */
public class JavascriptValueSource implements ValueSource, VectorSource, Initialisable {

  private ValueType type;

  private String script;

  private String scriptName = "customScript";

  private transient Script compiledScript;

  public JavascriptValueSource() {

  }

  public JavascriptValueSource(ValueType type, String script) {
    if(type == null) throw new IllegalArgumentException("type cannot be null");
    if(script == null) throw new IllegalArgumentException("script cannot be null");
    this.type = type;
    this.script = script;
  }

  public String getScriptName() {
    return scriptName;
  }

  public void setScriptName(String name) {
    this.scriptName = name;
  }

  public String getScript() {
    return script;
  }

  @Override
  public Value getValue(final ValueSet valueSet) {
    if(getValueType() == null) {
      throw new IllegalStateException("valueType must be set before calling getValue().");
    }
    if(compiledScript == null) {
      throw new IllegalStateException("script hasn't been compile. Call initialise() before calling getValue().");
    }
    return ((Value) ContextFactory.getGlobal().call(new ValueSetEvaluationContextAction(valueSet)));
  }

  @Override
  public VectorSource asVectorSource() {
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
    if(getValueType() == null) {
      throw new IllegalStateException("valueType must be set before calling getValue().");
    }
    if(compiledScript == null) {
      throw new IllegalStateException("script hasn't been compile. Call initialise() before calling getValue().");
    }
    return ((Iterable<Value>) ContextFactory.getGlobal().call(new ValueVectorEvaluationContextAction(entities)));
  }

  @Override
  public ValueType getValueType() {
    return type;
  }

  @Override
  public void initialise() throws EvaluatorException {
    String script = getScript();
    if(script == null) {
      throw new NullPointerException("script cannot be null");
    }
    try {
      compiledScript = (Script) ContextFactory.getGlobal().call(new ContextAction() {
        @Override
        public Object run(Context cx) {
          return cx.compileString(getScript(), getScriptName(), 1, null);
        }
      });
    } catch(EvaluatorException e) {
      throw e;
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
   * <p>
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

    public Object run(Context ctx) {
      MagmaContext context = MagmaContext.asMagmaContext(ctx);
      // Don't pollute the global scope
      Scriptable scope = context.newLocalScope();

      enterContext(context, scope);
      Object result = eval(context, scope);
      return result;
    }

    void enterContext(MagmaContext context, Scriptable scope) {
      JavascriptValueSource.this.enterContext(context, scope);
    }

    abstract Object eval(MagmaContext context, Scriptable scope);

    Value asValue(Object value) {
      Value result = null;
      if(value == null) {
        result = isSequence() ? getValueType().nullSequence() : getValueType().nullValue();
      } else if(value instanceof ScriptableValue) {
        ScriptableValue scriptableValue = (ScriptableValue) value;
        if(scriptableValue.getValue().isSequence() != isSequence()) {
          throw new MagmaJsRuntimeException("The returned value is " + (isSequence() ? "" : "not ") + "expected to be a value sequence.");
        }
        result = scriptableValue.getValue();
      } else if(value instanceof Undefined) {
        result = isSequence() ? getValueType().nullSequence() : getValueType().nullValue();
      } else {
        if(isSequence()) {
          if(value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Value> values = new ArrayList<Value>(length);
            for(int i = 0; i < length; i++) {
              values.add(getValueType().valueOf(Array.get(value, i)));
            }
            result = getValueType().sequenceOf(values);
          } else {
            // Build a singleton sequence
            result = getValueType().sequenceOf(ImmutableList.of(getValueType().valueOf(value)));
          }
        } else {
          result = getValueType().valueOf(value);
        }
      }

      if(result.getValueType() != getValueType()) {
        // Convert types
        try {
          result = getValueType().convert(result);
        } catch(RuntimeException e) {
          throw new MagmaJsRuntimeException("Cannot convert value '" + result.toString() + "' to type '" + getValueType().getName() + "'");
        }
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
    Object eval(MagmaContext context, Scriptable scope) {
      return asValue(compiledScript.exec(context, scope));
    }

  }

  private final class ValueVectorEvaluationContextAction extends AbstractEvaluationContextAction {

    private final SortedSet<VariableEntity> entities;

    ValueVectorEvaluationContextAction(SortedSet<VariableEntity> entities) {
      this.entities = entities;
    }

    SortedSet<VariableEntity> getEntities(MagmaContext context) {
      if(entities == null) {
        return new TreeSet<VariableEntity>(context.peek(ValueTable.class).getVariableEntities());
      }
      return entities;
    }

    @Override
    void enterContext(MagmaContext context, Scriptable scope) {
      super.enterContext(context, scope);
      context.push(SortedSet.class, getEntities(context));
      context.push(VectorCache.class, new VectorCache());
    }

    @Override
    Object eval(final MagmaContext context, final Scriptable scope) {
      return Iterables.transform(getEntities(context), new Function<VariableEntity, Value>() {
        @Override
        public Value apply(VariableEntity from) {
          try {
            // We have to set the current thread's context because this code will be executed outside of the
            // ContextAction.
            ContextFactory.getGlobal().enterContext(context);
            context.push(VariableEntity.class, from);
            return asValue(compiledScript.exec(context, scope));
          } finally {
            context.peek(VectorCache.class).next();
            context.pop(VariableEntity.class);
            Context.exit();
          }
        }
      });
    }

  }

  public static class VectorCache {

    Map<VectorSource, VectorHolder> vectors = Maps.newHashMap();

    // Holds the current "row" of the evaluation.
    private int index = 0;

    void next() {
      index++;
    }

    // Returns the value of the current "row" for the specified vector
    public Value get(MagmaContext context, VectorSource source) {
      VectorHolder holder = vectors.get(source);
      if(holder == null) {
        holder = new VectorHolder(source.getValues(context.peek(SortedSet.class)).iterator());
        vectors.put(source, holder);
      }
      return holder.get(index);
    }
  }

  private static class VectorHolder {

    private final Iterator<Value> values;

    // The index of the value returned by values.next();
    private int nextIndex = 0;

    // Value of nextIndex - 1 (null after ctor)
    private Value currentValue;

    VectorHolder(Iterator<Value> values) {
      this.values = values;
    }

    /**
     * Returns the value of the "row" for this vector. This method will advance the iterator until we reach the
     * requested row. This is required because during evaluation, not all vectors involved in a script are incremented
     * during evaluation (due to 'if' statements in the script).
     * 
     * For example, in the following script:
     * 
     * <pre>
     * $('VAR1') ? $('VAR2') : $('VAR3')
     * 
     * <pre>
     * vectors for VAR2 and VAR3 are not incremented at the same "rate" as VAR1.
     */
    Value get(int index) {
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
