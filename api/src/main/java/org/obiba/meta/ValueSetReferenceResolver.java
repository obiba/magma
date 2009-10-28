package org.obiba.meta;

/**
 * Resolves a {@code ValueSetReference} to an object. The resolved type is implementation specific.
 */
public interface ValueSetReferenceResolver {

  /**
   * Returns true when this resolver can resolve the referenced value set to an implementation specific object.
   * @param reference the {@code ValueSetReference} to test.
   * @return true when this resolver will return a non-null value when {@code resolve} is invoked with the same
   * reference. The method should return false otherwise.
   */
  public boolean canResolve(ValueSetReference reference);

  public <T> T resolve(ValueSetReference reference);

}
