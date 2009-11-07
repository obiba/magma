package org.obiba.meta;

import java.util.Set;

/**
 * 
 */
public interface ValueSetProvider {

  public String getEntityType();

  public boolean isForEntityType(String entityType);

  public Set<VariableEntity> getVariableEntities();

  public ValueSet getValueSet(VariableEntity entity);

  public Set<Occurrence> loadOccurrences(ValueSet valueSet, Variable variable);

}
