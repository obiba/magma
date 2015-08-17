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

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.MagmaContextFactory;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavascriptMultiplexingStrategy implements DatasourceCopier.MultiplexingStrategy {
  private static final Logger log = LoggerFactory.getLogger(JavascriptMultiplexingStrategy.class);

  private static final String SCRIPT_NAME = "customScript";

  private final String script;

  private CompiledScript compiledScript;

  public JavascriptMultiplexingStrategy(String script) {
    this.script = script;
    initialise();
  }

  public void initialise() {
    if(script == null) {
      throw new NullPointerException("script cannot be null");
    }

    try {
      compiledScript = ((Compilable) MagmaContextFactory.getEngine()).compile(getScript());
    } catch(ScriptException e) {
      e.printStackTrace();
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
    if(compiledScript == null)
      throw new IllegalStateException("Script hasn't been compiled. Call initialise() before calling it.");

    MagmaContext magmaContext = MagmaContextFactory.createContext();
    Object value = magmaContext.exec(() -> {
      try {
        return compiledScript.eval(magmaContext);
      } catch(ScriptException e) {
        e.printStackTrace();
      }

      return null;
    });

    if(value instanceof String) return (String)value;

    if(value instanceof ScriptableValue) {
      ScriptableValue scriptable = (ScriptableValue) value;

      if(scriptable.getValueType().equals(TextType.get())) {
        Value scriptableValue = scriptable.getValue();

        return scriptableValue.isNull() ? null : (String)scriptableValue.getValue();
      }
    }

    return null;
  }

  public String getScriptName() {
    return SCRIPT_NAME;
  }
}
