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

import org.junit.Assert;
import org.junit.Test;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;

public abstract class AbstractValueTest extends MagmaTest {

  @SuppressWarnings("ConstantConditions")
  @Test(expected = RuntimeException.class)
  public void test_ctorThrowsWhenValueTypeIsNull() {
    new Value(null, null);
  }

  @Test
  public void test_copy_returnsACopyThatIsEqual() {
    Value value = testValue();
    Value copy = value.copy();
    assertThat(copy).isEqualTo(value);
  }

  @Test
  public void test_getValue_returnsTheValue() {
    Value value = testValue();
    assertThat(value.getValue()).isEqualTo(testObject());
  }

  @Test
  public void test_getValue_returnsNullWhenIsNullIsTrue() {
    Value value = TextType.get().nullValue();

    assertThat(value.isNull()).isTrue();

    try {
      value.getValue();
      Assert.fail("Should throw NullPointerException");
    } catch(NullPointerException ignored) {
    }
  }

  @Test
  public void test_equals() {
    Value value = testValue();
    Value another = testValue();

    assertThat(value.equals(value)).isTrue();
    assertThat(value.equals(another)).isTrue();
    assertThat(another.equals(value)).isTrue();

    //noinspection ObjectEqualsNull
    assertThat(value.equals(null)).isFalse();
    assertThat(value.equals(testObject())).isFalse();
  }

  @Test
  public void test_hashCode() {
    Value value = testValue();
    Value another = testValue();

    assertThat(value.equals(another)).isTrue();
    assertThat(value.hashCode() == another.hashCode()).isTrue();
  }

  abstract protected Value testValue();

  abstract protected Object testObject();

}