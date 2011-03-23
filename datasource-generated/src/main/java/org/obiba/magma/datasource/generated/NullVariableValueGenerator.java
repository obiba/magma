package org.obiba.magma.datasource.generated;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;

class NullVariableValueGenerator extends GeneratedVariableValueSource {

  NullVariableValueGenerator(Variable variable) {
    super(variable);
  }

  @Override
  protected Value nextValue(Variable variable, GeneratedValueSet gvs) {
    return getValueType().nullValue();
  }

}
