package org.obiba.magma;

/**
 * Decorator design pattern.
 * @param <T>
 */
public interface Decorator<T> {

  /**
   * Decorate an object and return the result.
   * @param object
   * @return
   */
  T decorate(T object);

  /**
   * Release any resources that could have been associated to the decorated object.
   * @param object
   */
  void release(T object);

}
