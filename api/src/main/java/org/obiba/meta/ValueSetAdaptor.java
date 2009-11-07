package org.obiba.meta;

public interface ValueSetAdaptor {

  public <T> T adapt(Class<T> type, ValueSet valueSet);

}
