package org.obiba.magma.filter;

import java.util.Set;

import org.obiba.magma.Collection;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Occurrence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.AbstractCollectionWrapper;

public class FilteredCollection extends AbstractCollectionWrapper {

  private final CollectionFilterChain<VariableValueSource> variableFilterChain;

  private final CollectionFilterChain<VariableEntity> valueSetFilterChain;

  private Collection collection;

  public FilteredCollection(Collection collection, CollectionFilterChain<VariableValueSource> variableFilterChain, CollectionFilterChain<VariableEntity> valueSetFilterChain) {
    this.collection = collection;
    this.valueSetFilterChain = valueSetFilterChain;
    this.variableFilterChain = variableFilterChain;
  }

  @Override
  public Set<VariableEntity> getEntities(String entityType) {
    // TODO Auto-generated method stub
    return super.getEntities(entityType);
  }

  @Override
  public Set<String> getEntityTypes() {
    // TODO Auto-generated method stub
    return super.getEntityTypes();
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return super.getName();
  }

  @Override
  public Set<Variable> getVariables() {
    // TODO Auto-generated method stub
    return super.getVariables();
  }

  @Override
  public VariableValueSource getVariableValueSource(String entityType, String variableName) throws NoSuchVariableException {
    // TODO Auto-generated method stub
    return super.getVariableValueSource(entityType, variableName);
  }

  @Override
  public Set<VariableValueSource> getVariableValueSources(String entityType) {
    // TODO Auto-generated method stub
    return super.getVariableValueSources(entityType);
  }

  @Override
  public Collection getWrappedCollection() {
    // TODO Auto-generated method stub
    return super.getWrappedCollection();
  }

  @Override
  public Set<Occurrence> loadOccurrences(ValueSet valueSet, Variable variable) {
    // TODO Auto-generated method stub
    return super.loadOccurrences(valueSet, variable);
  }

  @Override
  public ValueSet loadValueSet(VariableEntity entity) {
    // TODO Auto-generated method stub
    return super.loadValueSet(entity);
  }

}
