package org.obiba.magma.filter;

public class StateEnvelope<T> {

  private FilterState state;

  private final T item;

  public StateEnvelope(T item) {
    this.item = item;
    this.state = FilterState.IN;
  }

  public FilterState getState() {
    return state;
  }

  public T getItem() {
    return item;
  }

  public void setState(FilterState state) {
    this.state = state;
  }

}
