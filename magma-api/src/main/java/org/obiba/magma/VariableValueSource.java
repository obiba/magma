package org.obiba.magma;

/**
 * Defines the contract for obtaining a {@link Value} of a specific {@link Variable} within a {@link ValueSet}
 */
public interface VariableValueSource extends ValueSource {

  String getName();

  Variable getVariable();

}
