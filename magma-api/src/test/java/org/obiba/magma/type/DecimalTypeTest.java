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
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

import static org.fest.assertions.api.Assertions.assertThat;

public class DecimalTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return DecimalType.get();
  }

  @Override
  Object getObjectForType() {
    return 78372.543543d;
  }

  @Override
  boolean isDateTime() {
    return false;
  }

  @Override
  boolean isNumeric() {
    return true;
  }

  @Override
  Iterable<Class<?>> validClasses() {
    return ImmutableList.<Class<?>>of(double.class, Double.class, float.class, Float.class);
  }

  @Test
  public void testTrim() {
    Double result = (Double) getValueType().valueOf(" 1 ").getValue();
    assertThat(result.intValue()).isEqualTo(1);
  }

  @Test
  public void testFloat() {
    Double result = (Double) getValueType().valueOf(0.123F).getValue();
    assertThat(result.toString()).isEqualTo("0.123");
  }

  @Test
  public void test_compare_with_null() throws Exception {
    Value leftValue = getValueType().valueOf(42);
    Value rightValue = getValueType().nullValue();
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result).isEqualTo(1);
  }

  @Test
  public void testComma() {
    Double result = (Double) getValueType().valueOf("1,2").getValue();
    assertThat(result).isEqualTo(1.2);
  }

  @Test
  public void test_valueOf_Value() {
    Value textValue = TextType.get().valueOf("1.5");
    Value value = DecimalType.get().valueOf(textValue);
    Double result = (Double) value.getValue();
    assertThat(result).isEqualTo(1.5);
  }

  @Test
  public void test_valueOf_ValueSequence() {
    Value textValue = TextType.get().sequenceOf("1.5,2.2");
    Value value = DecimalType.get().valueOf(textValue);
    assertThat(value.isSequence()).isTrue();
    Double result = (Double) value.asSequence().get(0).getValue();
    assertThat(result).isEqualTo(1.5);
    result = (Double) value.asSequence().get(1).getValue();
    assertThat(result).isEqualTo(2.2);
  }

  @Test
  public void testParseScientificNotation() {
    Value leftValue = getValueType().valueOf("1.0003776E7");
    Value rightValue = getValueType().valueOf(10003776);
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result).isEqualTo(0);
  }

}
