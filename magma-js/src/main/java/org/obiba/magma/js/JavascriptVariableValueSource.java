package org.obiba.magma.js;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.mozilla.javascript.Scriptable;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.views.View;

public class JavascriptVariableValueSource extends JavascriptValueSource implements VariableValueSource {

  private final Variable variable;

  @Nullable
  private final ValueTable valueTable;

  public JavascriptVariableValueSource(Variable variable, @Nullable ValueTable valueTable) {
    super(variable.getValueType(), "");
    this.variable = variable;
    this.valueTable = valueTable;
  }

  public JavascriptVariableValueSource(Variable variable) {
    this(variable, null);
  }

  @Nonnull
  @Override
  public String getScript() {
    return variable.hasAttribute(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME) //
        ? variable.getAttributeStringValue(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME) //
        : "";
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

  @Nullable
  public ValueTable getValueTable() {
    if (valueTable.isView()) return ((View)valueTable).getWrappedValueTable();
    return valueTable;
  }

  @Override
  protected void enterContext(MagmaContext ctx, Scriptable scope) {
    super.enterContext(ctx, scope);
    if(valueTable != null) {
      ctx.push(ValueTable.class, getValueTable());
      if (valueTable.isView()) {
        ctx.push(View.class, (View)valueTable);
      }
    }
    ctx.push(Variable.class, variable);
  }

  @Override
  protected void exitContext(MagmaContext ctx) {
    super.exitContext(ctx);
    if(valueTable != null) {
      ctx.pop(ValueTable.class);
      if (valueTable.isView()) {
        ctx.pop(View.class);
      }
    }
    ctx.pop(Variable.class);
  }

}
