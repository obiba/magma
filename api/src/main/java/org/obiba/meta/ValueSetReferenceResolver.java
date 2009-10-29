package org.obiba.meta;

/**
 * Resolves a {@code ValueSetReference} to an object. The resolved type is implementation specific.
 */
public interface ValueSetReferenceResolver<T> {

  public T resolve(ValueSetReference reference);

}
