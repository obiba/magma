package org.obiba.magma;

import java.io.Closeable;

import javax.validation.constraints.NotNull;

public interface ValueTableWriter extends Closeable {

  VariableWriter writeVariables();

  @NotNull
  ValueSetWriter writeValueSet(@NotNull VariableEntity entity);

  interface VariableWriter extends Closeable {

    void writeVariable(@NotNull Variable variable);

    void removeVariable(@NotNull Variable variable);
  }

  interface ValueSetWriter extends Closeable {

    void writeValue(@NotNull Variable variable, Value value);
  }

}
