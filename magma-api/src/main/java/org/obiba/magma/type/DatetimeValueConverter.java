/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

  @SuppressWarnings("ConstantConditions")
  @Override
  public Value convert(Value value, ValueType to) {
    if(value.isNull()) return to.nullValue();
    if(to == DateType.get()) {
      Object dateObject = value.getValue();
      Date date;
      if (dateObject instanceof MagmaDate)
        date = ((MagmaDate)dateObject).asDate();
      else
        date = (Date) dateObject;
      return to.valueOf(date);
    }
    if(to == DateTimeType.get()) {
      MagmaDate date = (MagmaDate) value.getValue();
      return to.valueOf(date.asCalendar());
    }
    throw new IllegalArgumentException("unknown valueType '" + to.getName() + "'");
  }
}
