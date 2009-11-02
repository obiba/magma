package org.obiba.meta;

import java.util.Set;

public interface Collection {

  public String getName();

  public Set<String> getEntityTypes();

  public Set<ValueSetReference> getValueSetReferences(String entityType);

  public Set<OccurrenceReference> getOccurrenceReferences(ValueSetReference reference, Variable variable);

  public Set<VariableValueSource> getVariableValueSources(String entityType);

  public VariableValueSource getVariableValueSource(String entityType, String variableName) throws NoSuchVariableException;

  public Set<Variable> getVariables();

}
