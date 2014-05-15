package org.obiba.magma;

public interface VariableValueSourceWrapper extends VariableValueSource {

  VariableValueSource getWrapped();

}
