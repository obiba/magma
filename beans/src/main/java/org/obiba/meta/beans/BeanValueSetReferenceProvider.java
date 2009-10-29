package org.obiba.meta.beans;

import java.util.List;
import java.util.Set;

import org.obiba.meta.ValueSetReference;
import org.obiba.meta.support.AbstractValueSetReferenceProvider;
import org.obiba.meta.support.ValueSetReferenceBean;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.google.common.collect.ImmutableSet;

public abstract class BeanValueSetReferenceProvider extends AbstractValueSetReferenceProvider {

  private String entityIdentifierPropertyPath;

  private String valueSetIdentifierPropertyPath;

  public BeanValueSetReferenceProvider(String entityType, String entityIdentifierPropertyPath, String valueSetIdentifierPropertyPath) {
    super(entityType);
    this.entityIdentifierPropertyPath = entityIdentifierPropertyPath;
    this.valueSetIdentifierPropertyPath = valueSetIdentifierPropertyPath;
  }

  @Override
  public boolean contains(ValueSetReference reference) {
    return false;
  }

  @Override
  public Set<ValueSetReference> getValueSetReferences() {
    ImmutableSet.Builder<ValueSetReference> builder = new ImmutableSet.Builder<ValueSetReference>();
    for(Object bean : getBeans()) {
      BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(bean);

      Object entityId = bw.getPropertyValue(entityIdentifierPropertyPath);
      if(entityId == null) {
        throw new NullPointerException("entity identifier cannot be null");
      }
      Object valueSetId = bw.getPropertyValue(valueSetIdentifierPropertyPath);
      if(entityId == null) {
        throw new NullPointerException("valueSet identifier cannot be null");
      }
      builder.add(new ValueSetReferenceBean(getEntityType(), entityId.toString(), valueSetId.toString()));
    }
    return builder.build();
  }

  protected abstract List<?> getBeans();

}
