package org.obiba.magma.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class CollectionFilterChain<T> implements FilterChain<T> {

  private String entityType;

  @XStreamImplicit
  private List<Filter<T>> filters;

  @Override
  public Set<T> filter(Set<T> unfilteredSet) {

    Set<T> filteredSet = new HashSet<T>();
    for(T item : unfilteredSet) {
      StateEnvelope<T> envelope = new StateEnvelope<T>(item);

      for(Filter<T> filter : getFilters()) {
        envelope = filter.doIt(envelope);
      }

      if(envelope.getState().equals(FilterState.IN)) {
        filteredSet.add(item);
      } else if(envelope.getState().equals(FilterState.OUT)) {
        filteredSet.remove(item);
      }
    }

    return filteredSet;
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
