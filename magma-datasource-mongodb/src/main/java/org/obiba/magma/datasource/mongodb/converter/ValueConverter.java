/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb.converter;

import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.LocaleType;

import com.mongodb.BasicDBList;

public class ValueConverter {

  public static Object marshall(Variable variable, Value value) {
    if (value == null || value.isNull()) return null;

    if(variable.isRepeatable()) {
      BasicDBList list = new BasicDBList();
      for (Value val : value.asSequence().getValues()) {
        list.add(marshall(val));
      }
      return list;
    } else {
      return marshall(value);
    }
  }

  private static Object marshall(Value value) {
    if (value == null || value.isNull()) return null;
    Object rawValue = value.getValue();
    if (rawValue instanceof MagmaDate) {
      return ((MagmaDate)rawValue).asDate();
    }
    return value.getValueType().equals(LocaleType.get())  ? value.getValueType().toString(value) : value.getValue();
  }

}
