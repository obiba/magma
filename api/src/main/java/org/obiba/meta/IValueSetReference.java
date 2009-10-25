package org.obiba.meta;

/**
 * A reference to a set of values of an {@code IVariableEntity}. This interface marks the existence of such a set, but
 * does not define how this set is obtained.
 */
public interface IValueSetReference {

  public IVariableEntity getVariableEntity();

  public String getIdentifier();

  public <T> T resolve();

}
