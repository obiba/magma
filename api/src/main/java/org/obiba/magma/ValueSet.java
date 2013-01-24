package org.obiba.magma;

public interface ValueSet extends Timestamped {

  ValueTable getValueTable();

  VariableEntity getVariableEntity();

}
