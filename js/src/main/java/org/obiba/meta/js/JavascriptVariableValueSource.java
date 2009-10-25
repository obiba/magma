package org.obiba.meta.js;

import org.obiba.meta.IVariable;
import org.obiba.meta.IVariableValueSource;
import org.obiba.meta.Initialisable;

public class JavascriptVariableValueSource extends JavascriptValueSource implements IVariableValueSource, Initialisable {

  private IVariable variable;

  @Override
  public IVariable getVariable() {
    return variable;
  }

  @Override
  public void initialise() {
    super.setScript(variable.getAttribute("script"));
    super.initialise();
  }

}
