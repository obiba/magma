package org.obiba.magma.filter;

import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Collection;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.AbstractCollectionWrapper;

public class FilteredCollection extends AbstractCollectionWrapper {

  private CollectionFilterChain<ValueSet> entityFilterChain;

  private CollectionFilterChain<VariableValueSource> variableFilterChain;

  public FilteredCollection(Collection collection, CollectionFilterChain<VariableValueSource> variableFilterChain, CollectionFilterChain<ValueSet> entityFilterChain) {
    super(collection);
    this.variableFilterChain = variableFilterChain;
    this.entityFilterChain = entityFilterChain;
  }

  @Override
  public Set<VariableEntity> getEntities(String entityType) {
    Set<VariableEntity> entities = super.getEntities(entityType);

    Set<ValueSet> unfilteredSet = new HashSet<ValueSet>();
    for(VariableEntity variableEntity : entities) {
      unfilteredSet.add(super.loadValueSet(variableEntity));
    }

    Set<VariableEntity> filteredEntities = new HashSet<VariableEntity>();
    for(ValueSet valueSet : entityFilterChain.filter(unfilteredSet)) {
      filteredEntities.add(valueSet.getVariableEntity());
    }

    return filteredEntities;
  }

  @Override
  public Set<String> getEntityTypes() {
    Set<String> entityTypes = super.getEntityTypes();

    Set<VariableEntity> filteredEntities = new HashSet<VariableEntity>();
    for(String entityType : entityTypes) {
      filteredEntities.addAll(getEntities(entityType));
    }

    Set<String> filteredTypes = new HashSet<String>();
    for(VariableEntity variableEntity : filteredEntities) {
      filteredTypes.add(variableEntity.getType());
    }

    return filteredTypes;

  }

  @Override
  public Set<Variable> getVariables() {
    Set<String> entityTypes = getEntityTypes();

    Set<VariableValueSource> filteredValueSources = new HashSet<VariableValueSource>();
    for(String entityType : entityTypes) {
      filteredValueSources.addAll(getVariableValueSources(entityType));
    }

    Set<Variable> filteredVariables = new HashSet<Variable>();
    for(VariableValueSource variableValueSource : filteredValueSources) {
      filteredVariables.add(variableValueSource.getVariable());
    }

    return filteredVariables;
  }

  @Override
  public VariableValueSource getVariableValueSource(String entityType, String variableName) throws NoSuchVariableException {
    Set<VariableValueSource> unfilteredValueSource = new HashSet<VariableValueSource>();
    unfilteredValueSource.add(super.getVariableValueSource(entityType, variableName));

    Set<VariableValueSource> filteredValueSource = variableFilterChain.filter(unfilteredValueSource);
    if(filteredValueSource.size() == 0) {
      throw new NoSuchVariableException(getName(), variableName);
    }

    return (VariableValueSource) filteredValueSource.toArray()[0];

  }

  @Override
  public Set<VariableValueSource> getVariableValueSources(String entityType) {
    Set<VariableValueSource> unfilteredValueSources = new HashSet<VariableValueSource>();
    unfilteredValueSources.addAll(super.getVariableValueSources(entityType));
    return variableFilterChain.filter(unfilteredValueSources);
  }

  @Override
  public ValueSet loadValueSet(VariableEntity entity) {
    Set<ValueSet> unfilteredValueSet = new HashSet<ValueSet>();
    unfilteredValueSet.add(super.loadValueSet(entity));

    Set<ValueSet> filteredValueSet = entityFilterChain.filter(unfilteredValueSet);
    if(filteredValueSet.size() == 0) {
      return null;
    }

    return (ValueSet) filteredValueSet.toArray()[0];
  }

}
