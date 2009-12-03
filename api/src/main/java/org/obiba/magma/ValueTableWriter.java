package org.obiba.magma;

import java.io.Closeable;

public interface ValueTableWriter extends Closeable {

  public VariableWriter writeVariables(String entityType);

  public ValueSetWriter writeValueSet(VariableEntity entity);

  public interface VariableWriter extends Closeable {
    public void writeVariable(Variable variable);
  }

  public interface ValueSetWriter extends Closeable {
    public void writeValue(Variable variable, Value value);
  }

}
