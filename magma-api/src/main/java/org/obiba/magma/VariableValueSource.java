package org.obiba.magma;

import javax.validation.constraints.NotNull;

/**
 * Defines the contract for obtaining a {@link Value} of a specific {@link Variable} within a {@link ValueSet}
 */
public interface VariableValueSource extends ValueSource {

  @NotNull
  String getName();

  @NotNull
  Variable getVariable();

}
