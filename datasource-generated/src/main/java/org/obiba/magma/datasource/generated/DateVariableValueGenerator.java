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

class DateVariableValueGenerator extends AbstractMissingValueVariableValueGenerator {

  private final ValueSource minimum;

  private final ValueSource maximum;

  DateVariableValueGenerator(final Variable variable) {
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
    long minimum = minimumValue.isNull() ? Long.MIN_VALUE : getTime(minimumValue);
    long maximum = maximumValue.isNull() ? Long.MAX_VALUE : getTime(maximumValue);
    return getValueType()
        .valueOf(new Date(minimum == maximum ? minimum : gvs.dataGenerator.nextLong(minimum, maximum)));
  }

  private ValueSource makeSource(Variable variable, String scriptAttribute) {
    if(variable.hasAttribute(scriptAttribute)) {
      return new JavascriptValueSource(variable.getValueType(), variable.getAttributeStringValue(scriptAttribute));
    } else {
      return new NullValueSource(variable.getValueType());
    }
  }

  private long getTime(Value value) {
    if(value.getValueType() == DateTimeType.get()) {
      return ((Date) value.getValue()).getTime();
    } else if(value.getValueType() == DateType.get()) {
      return ((MagmaDate) value.getValue()).asDate().getTime();
    }
    throw new IllegalArgumentException("value is neither date nor datetime");
  }

}
