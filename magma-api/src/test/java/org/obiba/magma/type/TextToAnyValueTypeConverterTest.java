/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.type;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueType;
import org.obiba.magma.test.AbstractMagmaTest;

public class TextToAnyValueTypeConverterTest extends AbstractMagmaTest {
  @Test
  public void test_converterFor_returnsAConverter() {
    ValueConverter converter = ValueType.Factory.converterFor(TextType.get(), BooleanType.get());
    Assert.assertNotNull(converter);
  }

  @Test
  public void test_convert_true_boolean_text_to_boolean() {
    Value value = TextType.get().valueOf("trUe");
    Value converted = BooleanType.get().convert(value);
    Assert.assertTrue((Boolean) converted.getValue());
  }

  @Test
  public void test_convert_false_boolean_text_to_boolean() {
    Value value = TextType.get().valueOf("fAlse");
    Value converted = BooleanType.get().convert(value);
    Assert.assertTrue(!((Boolean) converted.getValue()));
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
    Assert.assertTrue((Long) converted.getValue() == 1);
  }

  @Test
  public void test_convert_1_text_to_decimal() {
    Value value = TextType.get().valueOf("1.1");
    Value converted = DecimalType.get().convert(value);
    Assert.assertTrue((Double) converted.getValue() == 1.1);
  }
}
