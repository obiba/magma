package org.obiba.magma.datasource.generated;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.NullValueSource;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;

class NumericVariableValueGenerator extends AbstractMissingValueVariableValueGenerator {

  private final ValueSource minimum;

  private final ValueSource maximum;

  private final ValueSource mean;

  private final ValueSource stddev;

  NumericVariableValueGenerator(final Variable variable) {
    super(variable);
    minimum = makeSource(variable, "minimum");
    maximum = makeSource(variable, "maximum");
    mean = makeSource(variable, DecimalType.get(), "mean");
    stddev = makeSource(variable, DecimalType.get(), "stddev");
    Initialisables.initialise(minimum, maximum, mean, stddev);
  }

  @Override
  protected Value nonMissingValue(Variable variable, GeneratedValueSet gvs) {
    return getInteger(gvs, minimum.getValue(gvs), maximum.getValue(gvs));
  }

  protected Value getInteger(GeneratedValueSet gvs, Value minimumValue, Value maximumValue) {
    Number minimum = minimumValue.isNull() ? Long.MIN_VALUE : (Number) minimumValue.getValue();
    Number maximum = maximumValue.isNull() ? Long.MAX_VALUE : (Number) maximumValue.getValue();

    Value meanValue = mean.getValue(gvs);
    Value stddevValue = stddev.getValue(gvs);
    if(meanValue.isNull() || stddevValue.isNull()) {
      if(getValueType() == IntegerType.get()) {
        return getValueType().valueOf(
            minimum.equals(maximum) ? minimum : gvs.dataGenerator.nextLong(minimum.longValue(), maximum.longValue()));
      } else if(getValueType() == DecimalType.get()) {
        return getValueType().valueOf(minimum.equals(maximum)
            ? minimum
            : gvs.dataGenerator.nextUniform(minimum.doubleValue(), maximum.doubleValue()));
      }
      throw new IllegalStateException();
    } else {
      double v = gvs.dataGenerator
          .nextGaussian(((Number) meanValue.getValue()).doubleValue(), ((Number) stddevValue.getValue()).doubleValue());
      // Make sure value is between absolute min and max
      v = Math.min(v, maximum.doubleValue());
      v = Math.max(v, minimum.doubleValue());
      return getValueType().valueOf(v);
    }
  }

  private ValueSource makeSource(Variable variable, String scriptAttribute) {
    return makeSource(variable, variable.getValueType(), scriptAttribute);
  }

  private ValueSource makeSource(Variable variable, ValueType type, String scriptAttribute) {
    if(variable.hasAttribute(scriptAttribute)) {
      return new JavascriptValueSource(type, variable.getAttributeStringValue(scriptAttribute));
    } else {
      return new NullValueSource(type);
    }
  }

}
