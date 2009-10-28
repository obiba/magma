package org.obiba.meta;

/**
 * Defines the contract for obtaining a {@link Value} of a specific {@link Variable} within a
 * {@link ValueSetReference}
 */
public interface VariableValueSource extends ValueSource {

  public Variable getVariable();

}
