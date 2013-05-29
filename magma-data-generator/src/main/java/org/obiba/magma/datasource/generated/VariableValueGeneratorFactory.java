package org.obiba.magma.datasource.generated;

import javax.annotation.Nonnull;

import org.obiba.magma.Variable;

interface VariableValueGeneratorFactory {

  GeneratedVariableValueSource newGenerator(@Nonnull Variable variable);

}
