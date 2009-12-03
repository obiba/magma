package org.obiba.magma.filter;


public interface FilterChain<T> {

  public T filter(T item);

  public String getEntityType();

}
