package org.obiba.magma.js;

import org.mozilla.javascript.Scriptable;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;

public class JavascriptVariableValueSource extends JavascriptValueSource implements VariableValueSource {  
  private Variable variable;
  
  private ValueTable valueTable;

  public JavascriptVariableValueSource(Variable variable, ValueTable valueTable) {
    this.variable = variable;
    this.valueTable = valueTable;
  }
  
  public JavascriptVariableValueSource(Variable variable) {
    this(variable, null);
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
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  public String getScriptName() {
    return variable.getName();
  }

  @Override
  protected void enterContext(MagmaContext ctx, Scriptable scope, ValueSet valueSet) {
    super.enterContext(ctx, scope, valueSet);
	
	if (valueTable != null) {
	  ctx.push(ValueTable.class, valueTable);
	}
    ctx.push(Variable.class, variable);
  }

  @Override
  protected void exitContext(MagmaContext ctx) {
    super.exitContext(ctx);
	
	if (valueTable != null) {
	  ctx.pop(ValueTable.class);
	}
    ctx.pop(Variable.class);
  }

  @Override
  public String getScript() {
    return variable.getAttribute(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME).getValue().toString();
  }

}
