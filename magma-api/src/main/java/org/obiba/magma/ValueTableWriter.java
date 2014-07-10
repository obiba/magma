package org.obiba.magma;

import javax.validation.constraints.NotNull;

public interface ValueTableWriter extends AutoCloseable {

  VariableWriter writeVariables();

  @NotNull
  ValueSetWriter writeValueSet(@NotNull VariableEntity entity);

  @Override
  void close();

  interface VariableWriter extends AutoCloseable {

    void writeVariable(@NotNull Variable variable);

    void removeVariable(@NotNull Variable variable);

    @Override
    void close();

  }

  interface ValueSetWriter extends AutoCloseable {

    void writeValue(@NotNull Variable variable, Value value);

    void remove();

    @Override
    void close();

  }

}
