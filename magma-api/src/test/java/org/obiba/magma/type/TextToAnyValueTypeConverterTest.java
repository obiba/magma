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

import org.junit.Test;
import org.obiba.magma.MagmaTest;
import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueType;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class TextToAnyValueTypeConverterTest extends MagmaTest {

  @Test
  public void test_converterFor_returnsAConverter() {
    ValueConverter converter = ValueType.Factory.converterFor(TextType.get(), BooleanType.get());
    assertThat(converter).isNotNull();
  }

  @Test
  public void test_convert_true_boolean_text_to_boolean() {
    Value value = TextType.get().valueOf("trUe");
    Value converted = BooleanType.get().convert(value);
    assertThat((Boolean) converted.getValue()).isTrue();
  }

  @Test
  public void test_convert_false_boolean_text_to_boolean() {
    Value value = TextType.get().valueOf("fAlse");
    Value converted = BooleanType.get().convert(value);
    assertThat((Boolean) converted.getValue()).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_convert_not_a_boolean_text_to_boolean() {
    Value value = TextType.get().valueOf("zouzou");
    BooleanType.get().convert(value);
  }

  @Test
  public void test_convert_1_text_to_long() {
    Value value = TextType.get().valueOf("1");
    Value converted = IntegerType.get().convert(value);
    assertThat((Long) converted.getValue() == 1).isTrue();
  }

  @Test
  public void test_convert_empty_text_to_long() {
    Value value = TextType.get().valueOf("");
    Value converted = IntegerType.get().convert(value);
    assertThat(converted.isNull()).isTrue();
  }

  @Test
  public void test_convert_null_text_to_long() {
    Value value = TextType.get().nullValue();
    Value converted = IntegerType.get().convert(value);
    assertThat(converted.isNull()).isTrue();
  }

  @Test
  public void test_convert_1_text_to_decimal() {
    Value value = TextType.get().valueOf("1.1");
    Value converted = DecimalType.get().convert(value);
    assertThat((Double) converted.getValue() == 1.1).isTrue();
  }

  @Test
  public void test_convert_date_text_to_date() {
    Value value = TextType.get().valueOf("1946-10-25");
    Value converted = DateType.get().convert(value);
    assertThat(converted.getValueType()).isEqualTo(DateType.get());
  }
}
