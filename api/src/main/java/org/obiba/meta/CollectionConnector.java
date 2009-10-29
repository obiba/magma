package org.obiba.meta;

import java.util.Set;

/**
 * Connects a collection to the actual {@code ValueSetReference} and {@code VarialeValueSource} instances for a given
 * {@code entityType}.
 */
public interface CollectionConnector extends ValueSetReferenceProvider {

  public VariableValueSource getVariableValueSource(String name);

  public boolean hasVariableValueSource(String name);

  public Set<Variable> getVariables();

  public Set<VariableValueSource> getVariableValueSources();

}
