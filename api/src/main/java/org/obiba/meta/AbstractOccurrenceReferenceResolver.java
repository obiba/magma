package org.obiba.meta;

public abstract class AbstractOccurrenceReferenceResolver<T> implements OccurrenceReferenceResolver<T> {
  @Override
  public T resolve(ValueSetReference reference) {
    if(reference instanceof OccurrenceReference) {
      return resolveOccurrence((OccurrenceReference) reference);
    }
    throw new NoSuchValueSetException(reference, "Can only resolve OccurrenceReference instances.");
  }

  protected abstract T resolveOccurrence(OccurrenceReference occurrence);
}
