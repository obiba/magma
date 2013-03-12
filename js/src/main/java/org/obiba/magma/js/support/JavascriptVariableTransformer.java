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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Variable;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.support.DatasourceCopier.VariableTransformer;
import org.obiba.magma.type.TextType;

/**
 *
 */
public class JavascriptVariableTransformer implements VariableTransformer {

  private String scriptName = "customScript";

  private String script;

  private Script compiledScript;

  public JavascriptVariableTransformer(String script) {
    super();
    this.script = script;
    initialise();
  }

  @Override
  public Variable transform(final Variable variable) {
    String newName = ((String) ContextFactory.getGlobal().call(new ContextAction() {
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

    return Variable.Builder.sameAs(variable).name(newName != null ? newName : variable.getName()).build();
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

  public String getScriptName() {
    return scriptName;
  }

  public String getScript() {
    return script;
  }

}
