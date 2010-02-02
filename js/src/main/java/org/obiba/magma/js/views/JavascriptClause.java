package org.obiba.magma.js.views;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.WhereClause;

public class JavascriptClause implements Initialisable, SelectClause, WhereClause {
  //
  // Instance Variables
  //

  private String scriptName = "customScript";

  private String script;

  private Script compiledScript;

  //
  // Constructors
  //

  /**
   * No-arg constructor for XStream.
   */
  public JavascriptClause() {

  }

  public JavascriptClause(String script) {
    this.script = script;
  }

  //
  // Initialisable Methods
  //

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

  //
  // SelectClause Methods
  //

  @Override
  public boolean select(final Variable variable) {
    if(compiledScript == null) {
      throw new IllegalStateException("script hasn't been compiled. Call initialise() before calling where().");
    }

    return ((Boolean) ContextFactory.getGlobal().call(new ContextAction() {
      public Object run(Context ctx) {
        MagmaContext context = MagmaContext.asMagmaContext(ctx);
        // Don't pollute the global scope
        Scriptable scope = new ScriptableVariable(context.newLocalScope(), variable);

        Object value = compiledScript.exec(ctx, scope);

        if(value instanceof Boolean) {
          return value;
        }
        if(value instanceof ScriptableValue) {
          ScriptableValue scriptable = (ScriptableValue) value;
          if(scriptable.getValueType().equals(BooleanType.get())) {
            return scriptable.getValue().getValue();
          }
        }
        return false;
      }
    }));
  }

  //
  // WhereClause Methods
  //

  @Override
  public boolean where(final ValueSet valueSet) {
    if(compiledScript == null) {
      throw new IllegalStateException("script hasn't been compiled. Call initialise() before calling where().");
    }

    return ((Boolean) ContextFactory.getGlobal().call(new ContextAction() {
      public Object run(Context ctx) {
        MagmaContext context = MagmaContext.asMagmaContext(ctx);
        // Don't pollute the global scope
        Scriptable scope = context.newLocalScope();

        enterContext(context, scope, valueSet);
        Object value = compiledScript.exec(ctx, scope);
        exitContext(context);

        if(value instanceof Boolean) {
          return value;
        }
        if(value instanceof ScriptableValue) {
          ScriptableValue scriptable = (ScriptableValue) value;
          if(scriptable.getValueType().equals(BooleanType.get())) {
            return scriptable.getValue().getValue();
          }
        }
        return false;
      }
    }));
  }

  //
  // Methods
  //

  public String getScriptName() {
    return scriptName;
  }

  public void setScriptName(String name) {
    this.scriptName = name;
  }

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
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
