package org.obiba.magma.beans;

import java.util.Collections;
import java.util.Set;

import org.obiba.magma.Collection;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Occurrence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractValueSetProvider;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractBeanValueSetProvider<T> extends AbstractValueSetProvider {

  private String entityIdentifierPropertyPath;

  private Set<OccurrenceProvider> occurrenceProviders = Collections.emptySet();

  public AbstractBeanValueSetProvider(String entityType, String entityIdentifierPropertyPath) {
    super(entityType);
    this.entityIdentifierPropertyPath = entityIdentifierPropertyPath;
  }

  public String getEntityIdentifierPropertyPath() {
    return entityIdentifierPropertyPath;
  }

  public void setOccurrenceProviders(Set<OccurrenceProvider> occurrenceProviders) {
    if(occurrenceProviders == null) {
      throw new IllegalArgumentException("occurrenceProviders cannot be null");
    }
    this.occurrenceProviders = occurrenceProviders;
  }

  public Set<OccurrenceProvider> getOccurrenceProviders() {
    return occurrenceProviders;
  }

  @Override
  public ValueSet getValueSet(Collection collection, VariableEntity entity) {
    return new ValueSetBean(collection, entity);
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    ImmutableSet.Builder<VariableEntity> builder = new ImmutableSet.Builder<VariableEntity>();
    for(Object bean : loadBeans()) {
      BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(bean);

      Object entityId = bw.getPropertyValue(entityIdentifierPropertyPath);
      if(entityId == null) {
        throw new NullPointerException("entity identifier cannot be null");
      }
      builder.add(new VariableEntityBean(getEntityType(), entityId.toString()));
    }
    return builder.build();
  }

  @Override
  public Set<Occurrence> loadOccurrences(ValueSet valueSet, Variable variable) {
    for(OccurrenceProvider provider : occurrenceProviders) {
      if(provider.providesOccurrencesOf(variable)) {
        return provider.loadOccurrences(valueSet, variable);
      }
    }
    throw new NoSuchValueSetException(valueSet.getVariableEntity(), "No OccurrenceProvider for Variable " + variable);
  }

  /**
   * 
   * @return
   */
  protected abstract Iterable<T> loadBeans();

}
