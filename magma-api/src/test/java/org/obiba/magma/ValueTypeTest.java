/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma;

import java.util.Locale;

import org.junit.Test;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableList;

import static org.fest.assertions.api.Assertions.assertThat;

public class ValueTypeTest extends MagmaTest {

  @Test
  public void test_factory_forName() {
    for(ValueType type : ImmutableList
        .of(TextType.get(), IntegerType.get(), DateType.get(), DateTimeType.get(), BinaryType.get(),
            BooleanType.get())) {
      assertThat(type == ValueType.Factory.forName(type.getName())).isTrue();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_factory_forName_unknownTypeThrowsAnException() {
    ValueType.Factory.forName("no such type");
  }

  @Test
  public void test_factory_forClass() {
    testForClass(TextType.get(), String.class);
    testForClass(IntegerType.get(), int.class, Integer.class, long.class, Long.class);
    testForClass(DecimalType.get(), double.class, Double.class, float.class, Float.class);
    testForClass(BooleanType.get(), boolean.class, Boolean.class);
    testForClass(LocaleType.get(), Locale.class);
    testForClass(BinaryType.get(), byte[].class);
  }

  @Test
  public void test_factory_newValue() {
    assertThat(TextType.get().valueOf("A Value")).isEqualTo(ValueType.Factory.newValue("A Value"));
  }

  @SuppressWarnings("ConstantConditions")
  @Test(expected = IllegalArgumentException.class)
  public void test_factory_newValue_doesNotAcceptNull() {
    ValueType.Factory.newValue(null);
  }

  private void testForClass(ValueType type, Class<?>... validClasses) {
    for(Class<?> c : validClasses) {
      assertThat(type == ValueType.Factory.forClass(c)).isTrue();
    }
  }
}
