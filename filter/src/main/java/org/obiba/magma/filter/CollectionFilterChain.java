package org.obiba.magma.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class CollectionFilterChain<T> implements FilterChain<T> {

  private String entityType;

  @XStreamImplicit
  private List<Filter<T>> filters;

  @Override
  public Set<T> filter(Set<T> unfilteredSet) {
    // TODO Auto-generated method stub
    return null;
  }

  private List<Filter<T>> getFilters() {
    return filters;
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  public void addFilter(Filter<T> filter) {
    if(filters == null) filters = new ArrayList<Filter<T>>();
    filters.add(filter);
  }

}
