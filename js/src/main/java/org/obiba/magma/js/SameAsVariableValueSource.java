package org.obiba.magma.js;

import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public class SameAsVariableValueSource extends JavascriptVariableValueSource {

  public static final String SAME_AS_ATTRIBUTE_NAME = "sameAs";

  private final ValueTable valueTable;

  public SameAsVariableValueSource(Variable variable, ValueTable valueTable) {
    super(variable);
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    this.valueTable = valueTable;
  }

  @Override
  public String getScript() {
    try {
      return super.getScript();
    } catch(NoSuchAttributeException e) {
      return new StringBuilder().append("$('").append(getSameAs()).append("')").toString();
    }
  }

  public String getSameAs() {
    return super.getVariable().getAttribute(SAME_AS_ATTRIBUTE_NAME).getValue().toString();
  }

  @Override
  public Variable getVariable() {
    Variable original = valueTable.getVariable(getSameAs());
    Variable derived = super.getVariable();
    return Variable.Builder.sameAs(original).overrideWith(derived).build();
  }

}
