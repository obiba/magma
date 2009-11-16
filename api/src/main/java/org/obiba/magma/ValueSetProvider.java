package org.obiba.magma;

import java.util.Set;

/**
 * 
 */
public interface ValueSetProvider {

  public String getEntityType();

  public boolean isForEntityType(String entityType);

  public Set<VariableEntity> getVariableEntities();

  public ValueSet getValueSet(Collection collection, VariableEntity entity);

  public Set<Occurrence> loadOccurrences(ValueSet valueSet, Variable variable);

}
