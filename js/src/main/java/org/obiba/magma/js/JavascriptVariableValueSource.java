package org.obiba.magma.js;

import org.mozilla.javascript.Scriptable;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;

public class JavascriptVariableValueSource extends JavascriptValueSource implements VariableValueSource {

  private final Variable variable;

  private final ValueTable valueTable;

  public JavascriptVariableValueSource(Variable variable, ValueTable valueTable) {
    super(variable.getValueType(), "");
    this.variable = variable;
    this.valueTable = valueTable;
  }

  public JavascriptVariableValueSource(Variable variable) {
    this(variable, null);
  }

  @Override
  public String getScript() {
    return variable.getAttribute(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME).getValue().toString();
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Override
  protected boolean isSequence() {
    return variable.isRepeatable();
  }

  @Override
  public String getScriptName() {
    return variable.getName();
  }

  public ValueTable getValueTable() {
    return valueTable;
  }

  @Override
  protected void enterContext(MagmaContext ctx, Scriptable scope) {
    super.enterContext(ctx, scope);

    if(valueTable != null) {
      ctx.push(ValueTable.class, valueTable);
    }
    ctx.push(Variable.class, variable);
  }

  @Override
  protected void exitContext(MagmaContext ctx) {
    super.exitContext(ctx);

    if(valueTable != null) {
      ctx.pop(ValueTable.class);
    }
    ctx.pop(Variable.class);
  }

}
