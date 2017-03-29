/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueType;

/**
 * Converts {@code integer} to {@code decimal} and vice-versa
 */
public class NumericValueConverter implements ValueConverter {

  @Override
  public boolean converts(ValueType from, ValueType to) {
    return from.isNumeric() && to.isNumeric();
  }

  @Override
  public Value convert(Value value, ValueType to) {
    // When converting decimal to integer, this will truncate the decimal places: 0.9 -> 0
    return value == null || value.isNull() ? to.nullValue() : to.valueOf(value.getValue());
  }

}
