package org.obiba.meta;

import java.util.Set;

public interface Collection {

  public String getName();

  public Set<String> getEntityTypes();

  public IValueSetReferenceProvider getValueSetProvider(String entityType);

  public Set<IValueSetReference> getValueSetReferences(String entityType);

  public Set<IVariableValueSource> getVariables(String entityType);

  public IVariableValueSource getVariable(String entityType, String variableName);

}
