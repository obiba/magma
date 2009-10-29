package org.obiba.meta.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.ValueType;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableValueSource;

public class JavascriptVariableValueSource extends JavascriptValueSource implements VariableValueSource {

  private Variable variable;

  public JavascriptVariableValueSource(Variable variable) {
    this.variable = variable;
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  protected void enterContext(Context ctx, Scriptable scope, ValueSetReference valueSetReference) {
    super.enterContext(ctx, scope, valueSetReference);
    ctx.putThreadLocal(Variable.class, variable);
  }

  @Override
  public void initialise() {
    super.setScript(variable.getAttribute("script"));
    super.initialise();
  }

}
