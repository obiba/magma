package org.obiba.meta;

/**
 * Resolves a {@code ValueSetReference} to an object. The resolved type is implementation specific.
 */
public interface OccurrenceReferenceResolver<T> {

  public T resolve(OccurrenceReference reference);

}
