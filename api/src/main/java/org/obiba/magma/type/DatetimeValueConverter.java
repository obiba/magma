package org.obiba.magma.type;

import java.util.Date;

import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueType;

/**
 * Converts {@code datetime} to {@code date} and vice-versa
 */
public class DatetimeValueConverter implements ValueConverter {

  @Override
  public boolean converts(ValueType from, ValueType to) {
    return from.isDateTime() && to.isDateTime();
  }

  @Override
  public Value convert(Value value, ValueType to) {
    if(to == DateType.get()) {
      Date date = (Date) value.getValue();
      return to.valueOf(date);
    } else if(to == DateTimeType.get()) {
      MagmaDate date = (MagmaDate) value.getValue();
      return to.valueOf(date.asCalendar());
    }
    throw new IllegalArgumentException("unkown valueType '" + to.getName() + "'");
  }
}
