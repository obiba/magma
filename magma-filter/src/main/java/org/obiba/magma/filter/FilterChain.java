package org.obiba.magma.filter;

public interface FilterChain<T> {

  T filter(T item);

  String getEntityType();

}
