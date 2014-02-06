package org.obiba.magma.js;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public class SameAsVariableValueSource extends JavascriptVariableValueSource {

  static final String SAME_AS_ATTRIBUTE_NAME = "sameAs";

  public SameAsVariableValueSource(Variable variable, ValueTable valueTable) {
    super(variable, valueTable);
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
  }

  @NotNull
  @Override
  public String getScript() {
    return super.getVariable().hasAttribute(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME)
        ? super.getScript()
        : "$('" + getSameAs() + "')";
  }

  public String getSameAs() {
    return super.getVariable().getAttribute(SAME_AS_ATTRIBUTE_NAME).getValue().toString();
  }

  @NotNull
  @Override
  public Variable getVariable() {
    Variable original = getValueTable().getVariable(getSameAs());
    Variable derived = super.getVariable();
    return Variable.Builder.sameAs(original).overrideWith(derived).build();
  }

}
