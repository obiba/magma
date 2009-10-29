package org.obiba.meta;

import java.util.Set;

public interface Collection extends Initialisable {

  public String getName();

  public Set<String> getEntityTypes();

  public Set<ValueSetReference> getValueSetReferences(String entityType);

  public Set<VariableValueSource> getVariableValueSources(String entityType);

  public VariableValueSource getVariableValueSource(String entityType, String variableName);

}
