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

import org.obiba.magma.Variable;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.MagmaContextFactory;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.support.DatasourceCopier.VariableTransformer;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class JavascriptVariableTransformer implements VariableTransformer {
  private static final Logger log = LoggerFactory.getLogger(JavascriptVariableTransformer.class);

  private static final String SCRIPT_NAME = "customScript";

  private final String script;

  private CompiledScript compiledScript;

  public JavascriptVariableTransformer(String script) {
    this.script = script;
    initialise();
  }

  @Override
  public Variable transform(final Variable variable) {
    String newName = null;
    Object value = null;
    MagmaContext magmaContext = MagmaContextFactory.createContext();

    value = magmaContext.exec(()-> {
      try {
        return compiledScript.eval(magmaContext);
      } catch(ScriptException e) {
        e.printStackTrace();
      }
      return null;
    });

    if(value instanceof String) {
      newName = (String)value;
    }

    ScriptableValue tmp =(ScriptableValue)value;

    if(tmp.getValueType().equals(TextType.get())) {
      newName = tmp.getValue().isNull() ? null : (String)tmp.getValue().getValue();
    }

    return Variable.Builder.sameAs(variable).name(newName != null ? newName : variable.getName()).build();
  }

  public void initialise() {
    if(script == null) {
      throw new NullPointerException("script cannot be null");
    }

    try {
      compiledScript = ((Compilable)MagmaContextFactory.getEngine()).compile(getScript());
    } catch(ScriptException e) {
      e.printStackTrace();
    }
  }

  public String getScriptName() {
    return SCRIPT_NAME;
  }

  public String getScript() {
    return script;
  }

}
