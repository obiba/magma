package org.obiba.magma;

import java.io.Closeable;

import javax.annotation.Nonnull;

public interface ValueTableWriter extends Closeable {

  VariableWriter writeVariables();

  @Nonnull
  ValueSetWriter writeValueSet(@Nonnull VariableEntity entity);

  interface VariableWriter extends Closeable {
    void writeVariable(@Nonnull Variable variable);
  }

  interface ValueSetWriter extends Closeable {
    void writeValue(@Nonnull Variable variable, Value value);
  }

}
