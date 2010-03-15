package org.obiba.magma.js.support;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.type.TextType;

public class JavascriptMultiplexingStrategy implements DatasourceCopier.MultiplexingStrategy {

  private String scriptName = "customScript";

  private String script;

  private Script compiledScript;

  public JavascriptMultiplexingStrategy(String script) {
    super();
    this.script = script;
    initialise();
  }

  public void initialise() {
    if(script == null) {
      throw new NullPointerException("script cannot be null");
    }

    try {
      this.compiledScript = (Script) ContextFactory.getGlobal().call(new ContextAction() {
        @Override
        public Object run(Context cx) {
          return cx.compileString(getScript(), getScriptName(), 1, null);
        }
      });
    } catch(EvaluatorException e) {
      throw e;
    }
  }

  public String getScript() {
    return script;
  }

  @Override
  public String multiplexValueSet(VariableEntity entity, Variable variable) {
    return multiplexVariable(variable);
  }

  @Override
  public String multiplexVariable(final Variable variable) {
    if(compiledScript == null) {
      throw new IllegalStateException("Script hasn't been compiled. Call initialise() before calling it.");
    }

    return ((String) ContextFactory.getGlobal().call(new ContextAction() {
      public Object run(Context ctx) {
        MagmaContext context = MagmaContext.asMagmaContext(ctx);
        // Don't pollute the global scope
        Scriptable scope = new ScriptableVariable(context.newLocalScope(), variable);

        Object value = compiledScript.exec(ctx, scope);

        if(value instanceof String) {
          return value;
        }
        if(value instanceof ScriptableValue) {
          ScriptableValue scriptable = (ScriptableValue) value;
          if(scriptable.getValueType().equals(TextType.get())) {
            return scriptable.getValue().getValue();
          }
        }
        return null;
      }
    }));
  }

  public String getScriptName() {
    return scriptName;
  }

}
