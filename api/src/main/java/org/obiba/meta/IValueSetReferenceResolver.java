package org.obiba.meta;

public interface IValueSetReferenceResolver<T> {

  public T resolveReference(IValueSetReference reference);

}
