/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.js.support;

import javax.annotation.Nullable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.type.TextType;

public class JavascriptMultiplexingStrategy implements DatasourceCopier.MultiplexingStrategy {

  private static final String SCRIPT_NAME = "customScript";

  private final String script;

  private Script compiledScript;

  public JavascriptMultiplexingStrategy(String script) {
    this.script = script;
    initialise();
  }

  public void initialise() {
    if(script == null) {
      throw new NullPointerException("script cannot be null");
    }

    compiledScript = (Script) ContextFactory.getGlobal().call(new ContextAction() {
      @Override
      public Object run(Context cx) {
        return cx.compileString(getScript(), getScriptName(), 1, null);
      }
    });
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

    return (String) ContextFactory.getGlobal().call(new ContextAction() {
      @Nullable
      @Override
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
            Value scriptableValue = scriptable.getValue();
            return scriptableValue.isNull() ? null : scriptableValue.getValue();
          }
        }
        return null;
      }
    });
  }

  public String getScriptName() {
    return SCRIPT_NAME;
  }

}
