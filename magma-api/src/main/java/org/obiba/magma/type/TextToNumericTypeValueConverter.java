/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import java.math.BigDecimal;

import com.google.common.base.Strings;
import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueType;

public class TextToNumericTypeValueConverter implements ValueConverter {

  @Override
  public boolean converts(ValueType from, ValueType to) {
    return from == TextType.get() && (to == IntegerType.get() || to == DecimalType.get());
  }

  @Override
  public Value convert(Value value, ValueType to) {
    if (value == null || value.isNull()) return to.nullValue();
    String valueStr = value.toString();
    if (valueStr == null) return to.nullValue();
    valueStr = valueStr.trim();
    if (valueStr.isEmpty()) return to.nullValue();
    return to.valueOf(new BigDecimal(valueStr));
  }

}
