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

import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueType;

public class TextToAnyTypeValueConverter implements ValueConverter {

  @Override
  public boolean converts(ValueType from, ValueType to) {
    return from == TextType.get();
  }

  @Override
  public Value convert(Value value, ValueType to) {
    return to.valueOf(value);
  }

}
