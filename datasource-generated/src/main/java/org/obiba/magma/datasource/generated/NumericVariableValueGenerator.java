package org.obiba.magma.datasource.generated;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.Variable;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.NullValueSource;

class NumericVariableValueGenerator extends AbstractMissingValueVariableValueGenerator {

  private final ValueSource minimum;

  private final ValueSource maximum;

  private final ValueSource mean;

  private final ValueSource stddev;

  NumericVariableValueGenerator(final Variable variable) {
    super(variable);
    minimum = makeSource(variable, "minimum");
    maximum = makeSource(variable, "maximum");
    mean = makeSource(variable, "mean");
    stddev = makeSource(variable, "stddev");
    Initialisables.initialise(minimum, maximum, mean, stddev);
  }

  @Override
  protected Value nonMissingValue(Variable variable, GeneratedValueSet gvs) {
    return getInteger(gvs, minimum.getValue(gvs), maximum.getValue(gvs));
  }

  protected Value getInteger(GeneratedValueSet gvs, Value minimumValue, Value maximumValue) {
    long minimum = minimumValue.isNull() ? Long.MIN_VALUE : (Long) minimumValue.getValue();
    long maximum = maximumValue.isNull() ? Long.MAX_VALUE : (Long) maximumValue.getValue();
    Value meanValue = mean.getValue(gvs);
    Value stddevValue = stddev.getValue(gvs);
    if(meanValue.isNull() || stddevValue.isNull()) {
      return getValueType().valueOf(minimum == maximum ? minimum : gvs.dataGenerator.nextLong(minimum, maximum));
    } else {
      double v = gvs.dataGenerator.nextGaussian(((Number) meanValue.getValue()).doubleValue(), ((Number) stddevValue.getValue()).doubleValue());
      // Make sure value is between absolute min and max
      v = Math.min(v, maximum);
      v = Math.max(v, minimum);
      return getValueType().valueOf(v);
    }
  }

  private ValueSource makeSource(Variable variable, String scriptAttribute) {
    if(variable.hasAttribute(scriptAttribute)) {
      return new JavascriptValueSource(variable.getValueType(), variable.getAttributeStringValue(scriptAttribute));
    } else {
      return new NullValueSource(variable.getValueType());
    }
  }

}
