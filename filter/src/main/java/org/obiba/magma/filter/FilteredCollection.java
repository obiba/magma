package org.obiba.magma.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Collection;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.AbstractCollectionWrapper;

public class FilteredCollection extends AbstractCollectionWrapper {

  private Map<String, CollectionFilterChain<ValueSet>> entityFilterChainMap;

  private Map<String, CollectionFilterChain<VariableValueSource>> variableFilterChainMap;

  public FilteredCollection(Collection collection, CollectionFilterChain<VariableValueSource> variableFilterChain, CollectionFilterChain<ValueSet> entityFilterChain) {
    super(collection);
    this.entityFilterChainMap = new HashMap<String, CollectionFilterChain<ValueSet>>();
    this.entityFilterChainMap.put(entityFilterChain.getEntityType(), entityFilterChain);
    this.variableFilterChainMap = new HashMap<String, CollectionFilterChain<VariableValueSource>>();
    this.variableFilterChainMap.put(variableFilterChain.getEntityType(), variableFilterChain);
  }

  public FilteredCollection(Collection collection, Map<String, CollectionFilterChain<VariableValueSource>> variableFilterChainMap, Map<String, CollectionFilterChain<ValueSet>> entityFilterChainMap) {
    super(collection);
    this.entityFilterChainMap = entityFilterChainMap;
    this.variableFilterChainMap = variableFilterChainMap;
  }

  public FilteredCollection(Collection collection, List<CollectionFilterChain<VariableValueSource>> variableFilterChainList, List<CollectionFilterChain<ValueSet>> entityFilterChainList) {
    super(collection);
    variableFilterChainMap = new HashMap<String, CollectionFilterChain<VariableValueSource>>();
    for(CollectionFilterChain<VariableValueSource> variableFilterChain : variableFilterChainList) {
      variableFilterChainMap.put(variableFilterChain.getEntityType(), variableFilterChain);
    }

    entityFilterChainMap = new HashMap<String, CollectionFilterChain<ValueSet>>();
    for(CollectionFilterChain<ValueSet> entityFilterChain : entityFilterChainList) {
      entityFilterChainMap.put(entityFilterChain.getEntityType(), entityFilterChain);
    }
  }

  @Override
  public Set<VariableEntity> getEntities(String entityType) {
    Set<VariableEntity> entities = super.getEntities(entityType);

    Set<ValueSet> filteredSet = new HashSet<ValueSet>();
    for(VariableEntity variableEntity : entities) {
      filteredSet.add(loadValueSet(variableEntity));
    }

    Set<VariableEntity> filteredEntities = new HashSet<VariableEntity>();
    for(ValueSet valueSet : filteredSet) {
      filteredEntities.add(valueSet.getVariableEntity());
    }

    return filteredEntities;
  }

  @Override
  public Set<String> getEntityTypes() {
    return entityFilterChainMap.keySet();
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

    Set<VariableValueSource> filteredValueSource = variableFilterChainMap.get(entityType).filter(unfilteredValueSource);
    if(filteredValueSource.size() == 0) {
      throw new NoSuchVariableException(getName(), variableName);
    }

    return (VariableValueSource) filteredValueSource.toArray()[0];

  }

  @Override
  public Set<VariableValueSource> getVariableValueSources(String entityType) {
    Set<VariableValueSource> unfilteredValueSources = new HashSet<VariableValueSource>();
    unfilteredValueSources.addAll(super.getVariableValueSources(entityType));
    return variableFilterChainMap.get(entityType).filter(unfilteredValueSources);
  }

  @Override
  public ValueSet loadValueSet(VariableEntity entity) {
    Set<ValueSet> unfilteredValueSet = new HashSet<ValueSet>();
    unfilteredValueSet.add(super.loadValueSet(entity));

    Set<ValueSet> filteredValueSet = entityFilterChainMap.get(entity.getType()).filter(unfilteredValueSet);
    if(filteredValueSet.size() == 0) {
      throw new NoSuchValueSetException(entity);
    }

    return (ValueSet) filteredValueSet.toArray()[0];
  }

}
