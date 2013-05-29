package org.obiba.magma.datasource.generated;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;

class NullValueGenerator extends GeneratedVariableValueSource {

  NullValueGenerator(Variable variable) {
    super(variable);
  }

  @Override
  protected Value nextValue(Variable variable, GeneratedValueSet gvs) {
    return getValueType().nullValue();
  }

}
