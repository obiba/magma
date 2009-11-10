package org.obiba.meta.beans;

import java.util.Set;

import org.obiba.meta.NoSuchValueSetException;
import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableEntity;
import org.obiba.meta.support.AbstractValueSetProvider;
import org.obiba.meta.support.ValueSetBean;
import org.obiba.meta.support.VariableEntityBean;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractBeanValueSetProvider<T> extends AbstractValueSetProvider {

  private String entityIdentifierPropertyPath;

  private Set<OccurrenceProvider> occurrenceProviders;

  public AbstractBeanValueSetProvider(String entityType, String entityIdentifierPropertyPath) {
    super(entityType);
    this.entityIdentifierPropertyPath = entityIdentifierPropertyPath;
  }

  public String getEntityIdentifierPropertyPath() {
    return entityIdentifierPropertyPath;
  }

  public void setOccurrenceProviders(Set<OccurrenceProvider> occurrenceProviders) {
    this.occurrenceProviders = occurrenceProviders;
  }

  public Set<OccurrenceProvider> getOccurrenceProviders() {
    return occurrenceProviders;
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
  public ValueSet getValueSet(VariableEntity entity) {
    return new ValueSetBean(this, entity);
  }

  @Override
  public Set<Occurrence> loadOccurrences(ValueSet valueSet, Variable variable) {
    for(OccurrenceProvider provider : occurrenceProviders) {
      if(provider.occurenceOf(variable)) {
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
