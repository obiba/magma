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

    // Get the list of ValueSets from the Collection, for the requested entityType.
    Set<VariableEntity> entities = collection.getEntities(entityType);
    Set<ValueSet> unfilteredSet = new HashSet<ValueSet>();
    for(VariableEntity variableEntity : entities) {
      unfilteredSet.add(collection.loadValueSet(variableEntity));
    }

    // Filter these ValueSets and retrieve the list of filtered VariableEntity from them.
    Set<VariableEntity> filteredEntities = new HashSet<VariableEntity>();
    for(ValueSet valueSet : entityFilterChain.filter(unfilteredSet)) {
      filteredEntities.add(valueSet.getVariableEntity());
    }

    return filteredEntities;
  }

  @Override
  public Set<String> getEntityTypes() {
    Set<String> entityTypes = collection.getEntityTypes();
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
    Set<String> entityTypes = collection.getEntityTypes();
    Set<VariableValueSource> unfilteredValueSources = new HashSet<VariableValueSource>();
    for(String entityType : entityTypes) {
      unfilteredValueSources.addAll(collection.getVariableValueSources(entityType));
    }

    Set<Variable> filteredVariables = new HashSet<Variable>();
    for(VariableValueSource variableValueSource : variableFilterChain.filter(unfilteredValueSources)) {
      filteredVariables.add(variableValueSource.getVariable());
    }

    return filteredVariables;
  }

  @Override
  public VariableValueSource getVariableValueSource(String entityType, String variableName) throws NoSuchVariableException {
    Set<VariableValueSource> unfilteredValueSources = new HashSet<VariableValueSource>();
    unfilteredValueSources.add(collection.getVariableValueSource(entityType, variableName));

    Set<VariableValueSource> filteredValueSources;
    filteredValueSources = variableFilterChain.filter(unfilteredValueSources);
    if(filteredValueSources.size() == 0) {
      throw new NoSuchVariableException(getName(), variableName);
    }

    return (VariableValueSource) filteredValueSources.toArray()[0];

  }

  @Override
  public Set<VariableValueSource> getVariableValueSources(String entityType) {
    Set<VariableValueSource> unfilteredValueSources = new HashSet<VariableValueSource>();
    unfilteredValueSources.addAll(collection.getVariableValueSources(entityType));
    return variableFilterChain.filter(unfilteredValueSources);
  }

  @Override
  public ValueSet loadValueSet(VariableEntity entity) {
    Set<ValueSet> unfilteredSet = new HashSet<ValueSet>();
    unfilteredSet.add(collection.loadValueSet(entity));
    return (ValueSet) entityFilterChain.filter(unfilteredSet).toArray()[0];
  }

}
