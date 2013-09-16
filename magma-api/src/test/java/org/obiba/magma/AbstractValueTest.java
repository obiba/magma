/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma;

import org.junit.Test;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.TextType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public abstract class AbstractValueTest extends AbstractMagmaTest {

  public AbstractValueTest() {
  }

  @SuppressWarnings("ConstantConditions")
  @Test(expected = RuntimeException.class)
  public void test_ctorThrowsWhenValueTypeIsNull() {
    new Value(null, null);
  }

  @Test
  public void test_copy_returnsACopyThatIsEqual() {
    Value value = testValue();
    Value copy = value.copy();

    assertThat(copy, is(value));
  }

  @Test
  public void test_getValue_returnsTheValue() {
    Value value = testValue();

    assertThat(value.getValue(), is(testObject()));
  }

  @Test
  public void test_getValue_returnsNullWhenIsNullIsTrue() {
    Value value = TextType.get().nullValue();

    assertThat(value.isNull(), is(true));
    assertThat(value.getValue(), nullValue());
  }

  @Test
  public void test_equals() {
    Value value = testValue();
    Value another = testValue();

    assertThat(value.equals(value), is(true));
    assertThat(value.equals(another), is(true));
    assertThat(another.equals(value), is(true));

    //noinspection ObjectEqualsNull
    assertThat(value.equals(null), is(false));
    assertThat(value.equals(testObject()), is(false));
  }

  @Test
  public void test_hashCode() {
    Value value = testValue();
    Value another = testValue();

    assertThat(value.equals(another), is(true));
    assertThat(value.hashCode() == another.hashCode(), is(true));
  }

  abstract protected Value testValue();

  abstract protected Object testObject();

}