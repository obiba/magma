package org.obiba.meta;

import java.util.Set;

/**
 * Connects a collection to the actual {@code ValueSetReference} and {@code VarialeValueSource} instances for a given
 * {@code entityType}. The {@code CollectionConnector} is able to resolve any {@code ValueSetReference} to
 */
public interface CollectionConnector extends ValueSetReferenceResolver {

  public String getEntityType();

  public boolean isForEntityType(String entityType);

  public Set<ValueSetReference> getValueSetReferences();

  public VariableValueSource getVariableValueSource(String name);

  public boolean hasVariableValueSource(String name);

  public Set<Variable> getVariables();

  public Set<VariableValueSource> getVariableValueSources();

}
