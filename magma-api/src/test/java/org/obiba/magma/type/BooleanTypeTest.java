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

import com.google.common.collect.ImmutableList;
import org.obiba.magma.Value;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings("ReuseOfLocalVariable")
public class BooleanTypeTest extends BaseValueTypeTest {

  @Override
  BooleanType getValueType() {
    return BooleanType.get();
  }

  @Override
  Object getObjectForType() {
    return Boolean.TRUE;
  }

  @Override
  boolean isDateTime() {
    return false;
  }

  @Override
  boolean isNumeric() {
    return false;
  }

  @Override
  Iterable<Class<?>> validClasses() {
    return ImmutableList.<Class<?>>of(boolean.class, Boolean.class);
  }

  @Test
  public void test_valueOf_Boolean() {
    assertThat(getValueType().valueOf(Boolean.TRUE)).isEqualTo(getValueType().trueValue());
    assertThat(getValueType().valueOf(Boolean.FALSE)).isEqualTo(getValueType().falseValue());
    assertThat(getValueType().valueOf((Boolean) null).isNull()).isTrue();
  }

  @Test
  public void test_valueOf_string() {
    assertThat(getValueType().valueOf((Object) "false")).isEqualTo(getValueType().falseValue());
    assertThat(getValueType().valueOf((Object) "true")).isEqualTo(getValueType().trueValue());
    assertThat(getValueType().valueOf((Object) "FALSE")).isEqualTo(getValueType().falseValue());
    assertThat(getValueType().valueOf((Object) "TRUE")).isEqualTo(getValueType().trueValue());
    assertThat(getValueType().valueOf((Object) "F")).isEqualTo(getValueType().falseValue());
    assertThat(getValueType().valueOf((Object) "T")).isEqualTo(getValueType().trueValue());
  }

  @Test
  public void test_not_true() {
    assertThat(getValueType().not(getValueType().trueValue())).isEqualTo(getValueType().falseValue());
  }

  @Test
  public void test_not_false() {
    assertThat(getValueType().not(getValueType().falseValue())).isEqualTo(getValueType().trueValue());
  }

  @Test
  public void test_not_null() {
    assertThat(getValueType().not(getValueType().nullValue())).isEqualTo(getValueType().nullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_not_onlyAcceptsBoolenType() {
    getValueType().not(TextType.get().valueOf("not a boolean"));
  }

  @Test
  public void test_valueOf_Value() {
    Value textValue = TextType.get().valueOf("true");
    Value value = BooleanType.get().valueOf(textValue);
    Boolean result = (Boolean) value.getValue();
    assertThat(result).isEqualTo(Boolean.TRUE);
  }

  @Test
  public void test_valueOf_ValueSequence() {
    Value textValue = TextType.get().sequenceOf("true,false");
    Value value = BooleanType.get().valueOf(textValue);
    assertThat(value.isSequence()).isTrue();
    Boolean result = (Boolean) value.asSequence().get(0).getValue();
    assertThat(result).isEqualTo(Boolean.TRUE);
    result = (Boolean) value.asSequence().get(1).getValue();
    assertThat(result).isEqualTo(Boolean.FALSE);
  }
}
