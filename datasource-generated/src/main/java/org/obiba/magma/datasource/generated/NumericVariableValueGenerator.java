package org.obiba.magma.datasource.generated;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.Variable;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.support.Initialisables;

class NumericVariableValueGenerator extends GeneratedVariableValueSource {

  private final ValueSource minimum;

  private final ValueSource maximum;

  NumericVariableValueGenerator(Variable variable) {
    super(variable);
    if(variable.hasAttribute("minimum")) {
      minimum = new JavascriptValueSource(variable.getValueType(), variable.getAttributeStringValue("minimum"));
      Initialisables.initialise(minimum);
    } else {
      minimum = null;
    }

    if(variable.hasAttribute("maximum")) {
      maximum = new JavascriptValueSource(variable.getValueType(), variable.getAttributeStringValue("maximum"));
      Initialisables.initialise(maximum);
    } else {
      maximum = null;
    }

  }

  @Override
  protected Value nextValue(Variable variable, GeneratedValueSet gvs) {
    Value minimumValue = minimum != null ? minimum.getValue(gvs) : getValueType().nullValue();
    Value maximumValue = maximum != null ? maximum.getValue(gvs) : getValueType().nullValue();
    return getInteger(gvs, minimumValue, maximumValue);
  }

  protected Value getInteger(GeneratedValueSet gvs, Value minimumValue, Value maximumValue) {
    long minimum = minimumValue.isNull() ? Long.MIN_VALUE : (Long) minimumValue.getValue();
    long maximum = maximumValue.isNull() ? Long.MAX_VALUE : (Long) maximumValue.getValue();
    return getValueType().valueOf(minimum == maximum ? minimum : gvs.dataGenerator.nextLong(minimum, maximum));
  }

}
