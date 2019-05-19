/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.xstream.converter;

import org.obiba.magma.ValueType;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

public class ValueTypeConverter extends AbstractSingleValueConverter {

  public ValueTypeConverter() {
  }

  @Override
  @SuppressWarnings("rawtypes")
  public boolean canConvert(Class type) {
    return ValueType.class.isAssignableFrom(type);
  }

  @Override
  public String toString(Object obj) {
    return ((ValueType) obj).getName();
  }

  @Override
  public Object fromString(String str) {
    return ValueType.Factory.forName(str);
  }

}
