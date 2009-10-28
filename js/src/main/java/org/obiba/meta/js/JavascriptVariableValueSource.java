package org.obiba.meta.js;

import org.obiba.meta.Variable;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.Initialisable;

public class JavascriptVariableValueSource extends JavascriptValueSource implements VariableValueSource, Initialisable {

  private Variable variable;

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Override
  public void initialise() {
    super.setScript(variable.getAttribute("script"));
    super.initialise();
  }

}
