/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import java.util.Arrays;
import java.util.List;

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public final class Values {

  private Values() {

  }

  /**
   * Returns a {@code Function} instance that converts {@code from} to a {@code Value} instance using {@code
   * ValueType#valueOf(Object)}
   *
   * @param type the {@code ValueType} of the returned {@code Value} instances
   * @return a {@code Function} instance that can be used to convert Object instances to {@code Value} instances
   */
  public static Function<Object, Value> toValueFunction(final ValueType type) {
    return new Function<Object, Value>() {

      @Override
      public Value apply(Object from) {
        return type.valueOf(from);
      }

    };
  }

  /**
   * Returns an {@code Iterable} view of the {@code values} array converted to {@code Value} instances.
   *
   * @param type
   * @param values
   * @return
   */
  public static Iterable<Value> asValues(ValueType type, Object... values) {
    List<Value> valueList = Lists.newArrayList();
    if (values == null) return valueList;
    for (Object value : values) {
      valueList.add(type.valueOf(value));
    }
    return valueList;
  }

  /**
   * Returns a {@code ValueSequence} with the specified values as the sequence
   *
   * @param type
   * @param values
   * @return
   */
  public static Value asSequence(ValueType type, Object... values) {
    return type.sequenceOf(asValues(type, values));
  }
}
