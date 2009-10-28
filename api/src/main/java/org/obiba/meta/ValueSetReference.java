package org.obiba.meta;

/**
 * A reference to a set of values of a {@code VariableEntity}. This interface marks the existence of such a set, but
 * does not define how this set is obtained, {@code ValueSetReferenceResolver} can be used for this purpose.
 */
public interface ValueSetReference {

  public VariableEntity getVariableEntity();

  public String getIdentifier();

}
