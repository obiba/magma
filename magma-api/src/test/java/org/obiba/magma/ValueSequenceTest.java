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

import java.util.Comparator;

import org.junit.Test;
import org.obiba.magma.support.Values;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableList;

import static org.fest.assertions.api.Assertions.assertThat;

public class ValueSequenceTest extends AbstractValueTest {

  @Test
  public void test_isSequence() {
    Value value = testValue();
    assertThat(value.isSequence()).isTrue();
  }

  @Test
  public void test_asSequence() {
    Value value = testValue();
    ValueSequence sequence = value.asSequence();
    assertThat(value == sequence).isTrue();
  }

  @Test
  public void test_sort_sortsElementsInNaturalOrder() {
    ValueSequence value = testValue();
    ValueSequence sorted = value.sort();

    assertThat(value == sorted).isFalse();
    assertThat(sorted.getValue()).isEqualTo(ImmutableList.copyOf(Values.asValues(TextType.get(), "B", "C", "a")));
  }

  @Test
  public void test_sort_sortsUsingComparator() {
    ValueSequence value = testValue();
    ValueSequence sorted = value.sort(new Comparator<Value>() {

      @Override
      public int compare(Value o1, Value o2) {
        return String.CASE_INSENSITIVE_ORDER.compare((String) o1.getValue(), (String) o2.getValue());
      }
    });

    assertThat(value == sorted).isFalse();
    assertThat(sorted.getValue()).isEqualTo(ImmutableList.copyOf(Values.asValues(TextType.get(), "a", "B", "C")));
  }

  @Test
  public void test_getSize() {
    ValueSequence value = testValue();
    assertThat(value.getSize()).isEqualTo(3);
  }

  @Test
  public void test_get() {
    ValueSequence value = testValue();
    assertThat(value.get(0)).isEqualTo(TextType.get().valueOf("C"));
    assertThat(value.get(1)).isEqualTo(TextType.get().valueOf("B"));
    assertThat(value.get(2)).isEqualTo(TextType.get().valueOf("a"));
  }

  @Test(expected = RuntimeException.class)
  public void test_get_throwsWhenOutOfBound() {
    ValueSequence value = testValue();
    value.get(-1);
  }

  @Test
  public void test_contains() {
    ValueSequence value = testValue();
    assertThat(value.contains(TextType.get().valueOf("C"))).isTrue();
    assertThat(value.contains(TextType.get().valueOf("B"))).isTrue();
    assertThat(value.contains(TextType.get().valueOf("a"))).isTrue();
    assertThat(value.contains(TextType.get().valueOf("CBa"))).isFalse();
  }

  @Override
  protected ValueSequence testValue() {
    return TextType.get().sequenceOf(testObject());
  }

  @Override
  protected Iterable<Value> testObject() {
    return ImmutableList.copyOf(Values.asValues(TextType.get(), "C", "B", "a"));
  }
}
