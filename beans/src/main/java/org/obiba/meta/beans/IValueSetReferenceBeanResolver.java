package org.obiba.meta.beans;

import org.obiba.meta.IValueSetReferenceResolver;

public interface IValueSetReferenceBeanResolver extends IValueSetReferenceResolver<Object> {

  public Class<?> getResolvedBeanClass();

}
