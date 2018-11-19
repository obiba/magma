/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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

public class IntegerTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return IntegerType.get();
  }

  @Override
  Object getObjectForType() {
    return (long) 42;
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
    return ImmutableList.<Class<?>>of(int.class, Integer.class, long.class, Long.class);
  }

  @Test(expected = ClassCastException.class)
  public void testCompareWithRightArgumentOfWrongValueType() throws Exception {
    Value leftValue = getValueType().valueOf(getObjectForType());
    Value rightValue = TextType.get().valueOf("wrongType");
    getValueType().compare(leftValue, rightValue);
  }

  @Test(expected = ClassCastException.class)
  public void testCompareWithLeftArgumentOfWrongValueType() throws Exception {
    Value leftValue = TextType.get().valueOf("wrongType");
    Value rightValue = getValueType().valueOf(getObjectForType());
    getValueType().compare(leftValue, rightValue);
  }

  @Test
  public void testCompareWithLeftArgumentLessThanRightArgument() throws Exception {
    Value leftValue = getValueType().valueOf(40);
    Value rightValue = getValueType().valueOf(42);
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result).isEqualTo(-1);
  }

  @Test
  public void testCompareWithLeftArgumentGreaterThanRightArgument() throws Exception {
    Value leftValue = getValueType().valueOf(44);
    Value rightValue = getValueType().valueOf(42);
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result).isEqualTo(1);
  }

  @Test
  public void testCompareWithEqualArguments() throws Exception {
    Value leftValue = getValueType().valueOf(42);
    Value rightValue = getValueType().valueOf(42);
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result).isEqualTo(0);
  }


  @Test
  public void testParseScientificNotation() {
    Value leftValue = getValueType().valueOf("1.0003776E7");
    Value rightValue = getValueType().valueOf(10003776);
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result).isEqualTo(0);
  }

  @Test
  public void testParseScientificNotation2() {
    Value leftValue = getValueType().valueOf("1.0003776E5");
    Value rightValue = getValueType().valueOf(100037);
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result).isEqualTo(0);
  }

  @Test
  public void test_compare_with_null() throws Exception {
    Value leftValue = getValueType().valueOf(42);
    Value rightValue = getValueType().nullValue();
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result).isEqualTo(1);
  }

  @Test
  public void testTrim() {
    Long result = (Long) getValueType().valueOf(" 1 ").getValue();
    //noinspection ConstantConditions
    assertThat(result.intValue()).isEqualTo(1);
  }

  @Test
  public void test_valueOf_Value() {
    Value textValue = TextType.get().valueOf("1");
    Value value = IntegerType.get().valueOf(textValue);
    Long result = (Long) value.getValue();
    assertThat(result).isEqualTo(1);
  }

  @Test
  public void test_valueOf_ValueSequence() {
    Value textValue = TextType.get().sequenceOf("1,2");
    Value value = IntegerType.get().valueOf(textValue);
    assertThat(value.isSequence()).isTrue();
    Long result = (Long) value.asSequence().get(0).getValue();
    assertThat(result).isEqualTo(1);
    result = (Long) value.asSequence().get(1).getValue();
    assertThat(result).isEqualTo(2);
  }
}
