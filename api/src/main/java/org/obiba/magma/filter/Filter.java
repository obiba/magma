package org.obiba.magma.filter;

public interface Filter<T> {

  public StateEnvelope<T> doIt(StateEnvelope<T> stateEnvelope);

}
