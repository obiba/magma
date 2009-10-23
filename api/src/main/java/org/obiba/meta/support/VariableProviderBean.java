package org.obiba.meta.support;

import java.util.Collections;
import java.util.List;

import org.obiba.meta.IValueSetReference;
import org.obiba.meta.IVariable;
import org.obiba.meta.IVariableData;
import org.obiba.meta.IVariableProvider;
import org.obiba.meta.IVariableValueSource;
import org.obiba.meta.VariableData;

public class VariableProviderBean implements IVariableProvider {

  private String name;

  private IVariableValueSource source;

  private IVariable variable;

  @Override
  public List<IVariable> getVariables() {
    if(variable == null) {
      this.variable = new DelegatingVariable(source.getVariable()) {
        @Override
        public String getName() {
          return name;
        }
      };
    }
    return Collections.singletonList(variable);
  }

  @Override
  public IVariableData getData(IVariable variable, IValueSetReference valueSetReference) {
    return new VariableData(variable, valueSetReference, source.getValue(valueSetReference));
  }
}
