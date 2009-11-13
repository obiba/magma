package org.obiba.meta.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.obiba.meta.ValueSet;
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
  public String getScriptName() {
    return getVariable().getName();
  }

  @Override
  protected void enterContext(Context ctx, Scriptable scope, ValueSet valueSet) {
    super.enterContext(ctx, scope, valueSet);
    ctx.putThreadLocal(Variable.class, variable);
  }

  @Override
  public String getScript() {
    return variable.getAttribute(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME).getValue().toString();
  }

}
