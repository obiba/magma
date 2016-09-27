package org.obiba.magma.js;

import java.util.SortedSet;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.ValueTableWrapper;
import org.obiba.magma.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import static org.obiba.magma.js.JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME;

public class JavascriptVariableValueSource extends JavascriptValueSource implements VariableValueSource {

  private static final Logger log = LoggerFactory.getLogger(JavascriptVariableValueSource.class);

  @NotNull
  private final Variable variable;

  @Nullable
  private final ValueTable valueTable;

  @Nullable
  private Value lastScriptValidation;

  public JavascriptVariableValueSource(@NotNull Variable variable, @Nullable ValueTable valueTable) {
    super(variable.getValueType(), "");
    this.variable = variable;
    this.valueTable = valueTable;
  }

  public JavascriptVariableValueSource(@NotNull Variable variable) {
    this(variable, null);
  }

  @NotNull
  @Override
  public String getScript() {
    return variable.hasAttribute(SCRIPT_ATTRIBUTE_NAME) //
        ? variable.getAttributeStringValue(SCRIPT_ATTRIBUTE_NAME) //
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
    return valueTable != null && valueTable.isView() ? ((ValueTableWrapper) valueTable).getWrappedValueTable() : valueTable;
  }

  @NotNull
  @Override
  public Value getValue(ValueSet valueSet) {
    validateScript();
    return super.getValue(valueSet);
  }

  @Override
  public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
    validateScript();
    return super.getValues(entities);
  }

  public void validateScript() throws EvaluatorException {
    log.trace("Validate {} script", variable.getName());
    initialiseIfNot();
  }

  @Override
  protected void enterContext(MagmaContext context, Scriptable scope) {
    super.enterContext(context, scope);
    if (valueTable != null) {
      context.push(ValueTable.class, getValueTable());
      if(valueTable.isView() && valueTable instanceof View) {
        context.push(View.class, (View) valueTable);
      }
    }
    context.push(Variable.class, variable);
  }

  @Override
  protected void exitContext(MagmaContext context) {
    super.exitContext(context);
    if (valueTable != null) {
      context.pop(ValueTable.class);
      if(valueTable.isView()) {
        context.pop(View.class);
      }
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
