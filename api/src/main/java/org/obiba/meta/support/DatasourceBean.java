package org.obiba.meta.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.meta.Datasource;
import org.obiba.meta.IValueSetReference;
import org.obiba.meta.IValueSetReferenceProvider;
import org.obiba.meta.IVariableValueSource;
import org.obiba.meta.IVariableValueSourceProvider;

public class DatasourceBean implements Datasource {

  private String name;

  private IValueSetReferenceProvider referenceProvider;

  private List<IVariableValueSourceProvider> variableProviders;

  private List<IVariableValueSource> variableSources;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getEntityType() {
    return referenceProvider.getEntityType();
  }

  @Override
  public Set<IValueSetReference> getValueSetReferences() {
    return referenceProvider.getValueSetReferences();
  }

  @Override
  public Set<IVariableValueSource> getVariables() {
    Set<IVariableValueSource> variables = new HashSet<IVariableValueSource>();
    for(IVariableValueSourceProvider provider : variableProviders) {
      variables.addAll(provider.getVariables());
    }
    for(IVariableValueSource v : variableSources) {
      variables.add(v);
    }
    return Collections.unmodifiableSet(variables);
  }
}
