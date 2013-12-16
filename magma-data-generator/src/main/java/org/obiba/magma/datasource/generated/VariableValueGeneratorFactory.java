package org.obiba.magma.datasource.generated;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Variable;

interface VariableValueGeneratorFactory {

  GeneratedVariableValueSource newGenerator(@NotNull Variable variable);

}
