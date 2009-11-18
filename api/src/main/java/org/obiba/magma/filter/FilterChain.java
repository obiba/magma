package org.obiba.magma.filter;

import java.util.Set;

public interface FilterChain<T> {

  public Set<T> filter(Set<T> unfilteredSet);

  public String getEntityType();

}
