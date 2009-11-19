package org.obiba.magma.beans;

import java.util.Set;

import org.obiba.magma.Collection;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractValueSetProvider;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractBeanValueSetProvider<T> extends AbstractValueSetProvider {

  private String entityIdentifierPropertyPath;

  public AbstractBeanValueSetProvider(String entityType, String entityIdentifierPropertyPath) {
    super(entityType);
    this.entityIdentifierPropertyPath = entityIdentifierPropertyPath;
  }

  public String getEntityIdentifierPropertyPath() {
    return entityIdentifierPropertyPath;
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

  /**
   * 
   * @return
   */
  protected abstract Iterable<T> loadBeans();

}
