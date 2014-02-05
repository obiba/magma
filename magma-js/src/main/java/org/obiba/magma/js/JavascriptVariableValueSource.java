package org.obiba.magma.js;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.mozilla.javascript.Scriptable;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.ValueTableWrapper;
import org.obiba.magma.views.View;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

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

  @NotNull
  @Override
  public String getScript() {
    return variable.hasAttribute(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME) //
        ? variable.getAttributeStringValue(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME) //
        : "";
  }

  @Override
  public String getName() {
    return variable.getName();
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
    return valueTable != null && valueTable.isView()
        ? ((ValueTableWrapper) valueTable).getWrappedValueTable()
        : valueTable;
  }

  @Override
  protected void enterContext(MagmaContext context, Scriptable scope) {
    super.enterContext(context, scope);
    ReferenceNode referenceNode = null;
    if(valueTable == null) {
      referenceNode = new ReferenceNode(variable.getName());
    } else {
      context.push(ValueTable.class, getValueTable());
      if(valueTable.isView()) {
        context.push(View.class, (View) valueTable);
      }
      referenceNode = new ReferenceNode(variable.getVariableReference(valueTable));
    }
    context.push(Variable.class, variable);

    if(!context.has(ReferenceNode.class)) {
      context.push(ReferenceNode.class, referenceNode);
    }
  }

  @Override
  protected void exitContext(MagmaContext context) {
    super.exitContext(context);
    if(valueTable != null) {
      context.pop(ValueTable.class);
      if(valueTable.isView()) {
        context.pop(View.class);
      }
    }
    context.pop(Variable.class);
  }

  public static class ReferenceNode {

    @NotNull
    private final String variableRef;

    @Nullable
    private ReferenceNode caller;

    public ReferenceNode(@NotNull String variableRef) {
      this.variableRef = variableRef;
    }

    public void setCaller(@NotNull ReferenceNode caller) throws CircularVariableDependencyRuntimeException {
      checkCircularDependencies(caller, Sets.newHashSet(this));
      this.caller = caller;
    }

    private void checkCircularDependencies(@Nullable ReferenceNode node, Collection<ReferenceNode> callers)
        throws CircularVariableDependencyRuntimeException {
      if(node == null) return;
      if(callers.contains(node)) {
        throw new CircularVariableDependencyRuntimeException(node);
      }
      callers.add(node);
      checkCircularDependencies(node.getCaller(), callers);
    }

    @NotNull
    public String getVariableRef() {
      return variableRef;
    }

    @Nullable
    public ReferenceNode getCaller() {
      return caller;
    }

    @Override
    public boolean equals(Object o) {
      if(this == o) return true;
      if(!(o instanceof ReferenceNode)) return false;
      ReferenceNode that = (ReferenceNode) o;
      return variableRef.equals(that.variableRef);
    }

    @Override
    public int hashCode() {
      return variableRef.hashCode();
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this).omitNullValues().addValue(variableRef).add("caller", caller).toString();
    }
  }

}
