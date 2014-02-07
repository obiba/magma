package org.obiba.magma.datasource.generated;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.Variable;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;

class NumericValueGenerator extends AbstractMissingValueVariableValueGenerator {

  private static final long MIN_VALUE = -100;

  private static final long MAX_VALUE = 100;

  private final ValueSource minimum;

  private final ValueSource maximum;

  private final ValueSource mean;

  private final ValueSource stddev;

  NumericValueGenerator(Variable variable) {
    super(variable);
    minimum = makeSource(variable, "minimum", "min");
    maximum = makeSource(variable, "maximum", "max");
    mean = makeSource(variable, DecimalType.get(), "mean");
    stddev = makeSource(variable, DecimalType.get(), "stddev", "stdev");
    Initialisables.initialise(minimum, maximum, mean, stddev);
  }

  @Override
  protected Value nonMissingValue(Variable variable, GeneratedValueSet gvs) {
    return getInteger(gvs, getMinimum(gvs), getMaximum(gvs));
  }

  protected Value getInteger(GeneratedValueSet gvs, Number min, Number max) {
    Value meanValue = getMeanValue(gvs);
    Value stddevValue = getStdDevValue(gvs);
    if(meanValue.isNull() || stddevValue.isNull()) {
      if(getValueType() == IntegerType.get()) {
        return getValueType()
            .valueOf(min.equals(max) ? min : gvs.dataGenerator.nextLong(min.longValue(), max.longValue()));
      }
      if(getValueType() == DecimalType.get()) {
        return getValueType()
            .valueOf(min.equals(max) ? min : gvs.dataGenerator.nextUniform(min.doubleValue(), max.doubleValue()));
      }
      throw new IllegalStateException();
    }
    double value = gvs.dataGenerator
        .nextGaussian(((Number) meanValue.getValue()).doubleValue(), ((Number) stddevValue.getValue()).doubleValue());
    // Make sure value is between absolute min and max
    value = Math.min(value, max.doubleValue());
    value = Math.max(value, min.doubleValue());
    return getValueType().valueOf(value);
  }

  private ValueSource makeSource(Variable variable, String... scriptAttributes) {
    return makeSource(variable, variable.getValueType(), scriptAttributes);
  }

  private Number getMinimum(ValueSet valueSet) {
    try {
      Value minimumValue = minimum.getValue(valueSet);
      return minimumValue.isNull() ? MIN_VALUE : (Number) minimumValue.getValue();
    } catch(Exception ignored) {
      return MIN_VALUE;
    }
  }

  private Number getMaximum(ValueSet valueSet) {
    try {
      Value maximumValue = maximum.getValue(valueSet);
      return maximumValue.isNull() ? MIN_VALUE : (Number) maximumValue.getValue();
    } catch(Exception ignored) {
      return MAX_VALUE;
    }
  }

  private Value getMeanValue(ValueSet valueSet) {
    try {
      return mean.getValue(valueSet);
    } catch(Exception e) {
      return mean.getValueType().nullValue();
    }
  }

  private Value getStdDevValue(ValueSet valueSet) {
    try {
      return stddev.getValue(valueSet);
    } catch(Exception e) {
      return stddev.getValueType().nullValue();
    }
  }

}
