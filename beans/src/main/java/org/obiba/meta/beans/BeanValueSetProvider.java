package org.obiba.meta.beans;

import java.util.Set;

import org.obiba.meta.Collection;
import org.obiba.meta.NoSuchValueSetException;
import org.obiba.meta.ValueSet;
import org.obiba.meta.VariableEntity;
import org.obiba.meta.support.AbstractValueSetProvider;
import org.obiba.meta.support.ValueSetBean;
import org.obiba.meta.support.VariableEntityBean;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.google.common.collect.ImmutableSet;

public abstract class BeanValueSetProvider<T> extends AbstractValueSetProvider {

  private String entityIdentifierPropertyPath;

  public BeanValueSetProvider(String entityType, String entityIdentifierPropertyPath) {
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
  public ValueSet getValueSet(Collection collection, VariableEntity entity) {
    T bean = loadBean(entity);
    if(bean == null) {
      throw new NoSuchValueSetException(entity);
    }
    return buildValueSet(collection, entity, bean);
  }

  protected ValueSet buildValueSet(Collection collection, VariableEntity entity, T bean) {
    return new ValueSetBean(collection, entity, null, null);
  }

  protected abstract T loadBean(VariableEntity entity);

  protected abstract Iterable<T> loadBeans();

}
