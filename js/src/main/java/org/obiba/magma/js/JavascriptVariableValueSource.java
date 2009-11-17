package org.obiba.magma.js;

import org.mozilla.javascript.Scriptable;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;

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
  protected void enterContext(MagmaContext ctx, Scriptable scope, ValueSet valueSet) {
    super.enterContext(ctx, scope, valueSet);
    ctx.push(Variable.class, variable);
  }

  @Override
  protected void exitContext(MagmaContext ctx) {
    super.exitContext(ctx);
    ctx.pop(Variable.class);
  }

  @Override
  public String getScript() {
    return variable.getAttribute(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME).getValue().toString();
  }

}
