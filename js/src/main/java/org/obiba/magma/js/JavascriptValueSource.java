package org.obiba.magma.js;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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
import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

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
public class JavascriptValueSource implements ValueSource, Initialisable {

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
    Value evaluated = ((Value) ContextFactory.getGlobal().call(new ContextAction() {
      public Object run(Context ctx) {
        MagmaContext context = MagmaContext.asMagmaContext(ctx);
        // Don't pollute the global scope
        Scriptable scope = context.newLocalScope();

        enterContext(context, scope, valueSet);

        Object value = compiledScript.exec(ctx, scope);

        exitContext(context);
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
        return result;
      }
    }));

    if(evaluated.getValueType() != getValueType()) {
      // Convert types
      evaluated = getValueType().convert(evaluated);
    }
    return evaluated;
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
  protected void enterContext(MagmaContext ctx, Scriptable scope, ValueSet valueSet) {
    ctx.push(ValueSet.class, valueSet);
  }

  protected void exitContext(MagmaContext ctx) {
    ctx.pop(ValueSet.class);
  }
}
