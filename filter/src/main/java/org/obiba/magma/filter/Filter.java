package org.obiba.magma.filter;

public interface Filter<T> {

  StateEnvelope<T> doIt(StateEnvelope<T> stateEnvelope);

}
