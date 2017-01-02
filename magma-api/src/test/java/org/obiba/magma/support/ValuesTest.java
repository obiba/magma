/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.IntegerType;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import static org.fest.assertions.api.Assertions.assertThat;

public class ValuesTest extends AbstractMagmaTest {

  @Test
  public void test_toValueFunction_returnsFunctionForProperType() {
    Function<Object, Value> func = Values.toValueFunction(IntegerType.get());
    assertThat(func.apply(1)).isEqualTo(IntegerType.get().valueOf(1));
  }

  @Test
  public void test_toValueFunction_handlesNull() {
    Function<Object, Value> func = Values.toValueFunction(IntegerType.get());
    assertThat(func.apply(null)).isEqualTo(IntegerType.get().nullValue());
  }

  @Test
  public void test_asValues_returnsValuesOfProperType() {
    Iterable<Value> values = Values.asValues(IntegerType.get(), 1, 2);
    assertThat(values).isEqualTo(ImmutableList.<Value>of(IntegerType.get().valueOf(1), IntegerType.get().valueOf(2)));
  }

  @Test
  public void test_asValues_handlesNull() {
    Iterable<Value> values = Values.asValues(IntegerType.get(), 1, null);
    assertThat(values).isEqualTo(ImmutableList.<Value>of(IntegerType.get().valueOf(1), IntegerType.get().nullValue()));
  }

  @Test
  public void test_asValues_handlesSingleton() {
    Iterable<Value> values = Values.asValues(IntegerType.get(), 1);
    assertThat(Iterables.elementsEqual(values, ImmutableList.<Value>of(IntegerType.get().valueOf(1)))).isEqualTo(true);
  }

  @Test
  public void test_asValues_handlesEmtpyArray() {
    Iterable<Value> values = Values.asValues(IntegerType.get());
    assertThat(Iterables.elementsEqual(values, ImmutableList.<Value>of())).isEqualTo(true);
  }

}
