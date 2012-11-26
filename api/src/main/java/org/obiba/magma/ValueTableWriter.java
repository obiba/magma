package org.obiba.magma;

import java.io.Closeable;

public interface ValueTableWriter extends Closeable {

  VariableWriter writeVariables();

  ValueSetWriter writeValueSet(VariableEntity entity);

  interface VariableWriter extends Closeable {
    void writeVariable(Variable variable);
  }

  interface ValueSetWriter extends Closeable {
    void writeValue(Variable variable, Value value);
  }

}
