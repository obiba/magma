package org.obiba.meta;

/**
 * Defines the contract for obtaining a {@link Value} of a specific {@link IVariable} within a
 * {@link IValueSetReference}
 */
public interface IVariableValueSource extends IValueSource {

  public IVariable getVariable();

}
