package org.obiba.magma.datasource.generated;

import java.util.Date;

import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.Variable;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.NullValueSource;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;

class DateValueGenerator extends AbstractMissingValueVariableValueGenerator {

  // don't use Long.MIN_VALUE && Long.MAX_VALUE in order to keep year as 4 digits number

  private static final long DEFAULT_MIN_DATE = -30610202400l; // 01/01/1000

  private static final long DEFAULT_MAX_DATE = 253402236000l; // 12/31/9999

  private final ValueSource minimum;

  private final ValueSource maximum;

  DateValueGenerator(Variable variable) {
    super(variable);
    minimum = makeSource(variable, "minimum");
    maximum = makeSource(variable, "maximum");
    Initialisables.initialise(minimum, maximum);
  }

  @Override
  protected Value nonMissingValue(Variable variable, GeneratedValueSet gvs) {
    return getValue(gvs, minimum.getValue(gvs), maximum.getValue(gvs));
  }

  protected Value getValue(GeneratedValueSet gvs, Value minimumValue, Value maximumValue) {
    long min = getTime(minimumValue, DEFAULT_MIN_DATE);
    long max = getTime(maximumValue, DEFAULT_MAX_DATE);
    return getValueType().valueOf(new Date(min == max ? min : gvs.dataGenerator.nextLong(min, max)));
  }

  private ValueSource makeSource(Variable variable, String scriptAttribute) {
    return variable.hasAttribute(scriptAttribute) //
        ? new JavascriptValueSource(variable.getValueType(), variable.getAttributeStringValue(scriptAttribute)) //
        : new NullValueSource(variable.getValueType());
  }

  private long getTime(Value value, long defaultValue) {
    if(value.isNull()) {
      return defaultValue;
    }
    if(value.getValueType() == DateTimeType.get()) {
      return ((Date) value.getValue()).getTime();
    }
    if(value.getValueType() == DateType.get()) {
      return ((MagmaDate) value.getValue()).asDate().getTime();
    }
    throw new IllegalArgumentException("value is neither date nor datetime");
  }

}