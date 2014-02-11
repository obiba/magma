package org.obiba.magma.js;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.ValueTableWrapper;
import org.obiba.magma.views.View;

import com.google.common.base.Objects;

public class JavascriptVariableValueSource extends JavascriptValueSource implements VariableValueSource {

  @NotNull
  private final Variable variable;

  @NotNull
  private final ValueTable valueTable;

  public JavascriptVariableValueSource(@NotNull Variable variable, @NotNull ValueTable valueTable) {
    super(variable.getValueType(), "");
    this.variable = variable;
    this.valueTable = valueTable;
  }

  @NotNull
  @Override
  public String getScript() {
    return variable.hasAttribute(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME) //
        ? variable.getAttributeStringValue(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME) //
        : "";
  }

  @NotNull
  @Override
  public String getName() {
    return variable.getName();
  }

  @NotNull
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
    return valueTable.isView() ? ((ValueTableWrapper) valueTable).getWrappedValueTable() : valueTable;
  }

  @Override
  public void initialise() throws EvaluatorException {
    super.initialise();
    new VariableScriptValidator(variable, valueTable).validateScript();
  }

  @Override
  protected void enterContext(MagmaContext context, Scriptable scope) {
    super.enterContext(context, scope);
    context.push(ValueTable.class, getValueTable());
    if(valueTable.isView()) {
      context.push(View.class, (View) valueTable);
    }
    context.push(Variable.class, variable);
  }

  @Override
  protected void exitContext(MagmaContext context) {
    super.exitContext(context);
    context.pop(ValueTable.class);
    if(valueTable.isView()) {
      context.pop(View.class);
    }
    context.pop(Variable.class);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(variable, valueTable);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null || getClass() != obj.getClass()) return false;
    JavascriptVariableValueSource other = (JavascriptVariableValueSource) obj;
    return Objects.equal(variable, other.variable) && Objects.equal(valueTable, other.valueTable);
  }

}
