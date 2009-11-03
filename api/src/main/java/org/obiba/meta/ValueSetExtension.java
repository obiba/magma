package org.obiba.meta;

public interface ValueSetExtension<V extends ValueSet, T> {

  public T extend(V valueSet);

}
