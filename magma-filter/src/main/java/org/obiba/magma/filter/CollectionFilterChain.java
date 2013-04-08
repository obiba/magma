package org.obiba.magma.filter;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class CollectionFilterChain<T> implements FilterChain<T> {

  @XStreamOmitField
  private String entityType;

  @XStreamImplicit
  private List<Filter<T>> filters;

  public CollectionFilterChain(String entityType) {
    this.entityType = entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  @Override
  public T filter(T item) {
    StateEnvelope<T> envelope = new StateEnvelope<T>(item);

    // No filter, item is IN by default.
    if(getFilters() == null) return item;

    for(Filter<T> filter : getFilters()) {
      envelope = filter.doIt(envelope);
    }

    if(envelope.isState(FilterState.IN)) {
      return item;
    }
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
