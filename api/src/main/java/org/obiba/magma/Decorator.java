package org.obiba.magma;

public interface Decorator<T> {

  public T decorate(T object);

}
