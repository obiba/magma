package org.obiba.meta.beans;

import java.util.Set;

import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableEntity;
import org.obiba.meta.support.AbstractValueSetProvider;
import org.obiba.meta.support.VariableEntityBean;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractBeanValueSetProvider<T> extends AbstractValueSetProvider implements BeansValueSetProvider {

  private String entityIdentifierPropertyPath;

  public AbstractBeanValueSetProvider(String entityType, String entityIdentifierPropertyPath) {
    super(entityType);
    this.entityIdentifierPropertyPath = entityIdentifierPropertyPath;
  }

  public String getEntityIdentifierPropertyPath() {
    return entityIdentifierPropertyPath;
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
  public abstract ValueSet getValueSet(VariableEntity entity);

  @Override
  public abstract Set<Occurrence> loadOccurrences(ValueSet valueSet, Variable variable);

  protected abstract Iterable<T> loadBeans();

}
