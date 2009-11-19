package org.obiba.magma.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueType;

/**
 * A {@code ValueSource} implementation that uses a Javascript script to evaluate the {@code Value} to return.
 * <p>
 * Within the javascript engine, {@code Value} instances are represented by {@code ScriptableValue} host objects.
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

  private Script compiledScript;

  public String getScriptName() {
    return scriptName;
  }

  public void setScriptName(String name) {
    this.scriptName = name;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public String getScript() {
    return script;
  }

  public void setValueType(ValueType type) {
    this.type = type;
  }

  @Override
  public Value getValue(final ValueSet valueSet) {
    if(compiledScript == null) {
      throw new IllegalStateException("script hasn't been compile. Call initialise() before calling getValue().");
    }
    return ((ScriptableValue) ContextFactory.getGlobal().call(new ContextAction() {
      public Object run(Context ctx) {
        MagmaContext context = (MagmaContext) ctx;
        // Don't pollute the global scope
        Scriptable scope = context.newLocalScope();

        enterContext(context, scope, valueSet);

        Object value = compiledScript.exec(ctx, scope);

        exitContext(context);
        if(value instanceof Scriptable) {
          return value;
        }
        return new ScriptableValue(scope, getValueType().valueOf(value));
      }
    })).getValue();
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
