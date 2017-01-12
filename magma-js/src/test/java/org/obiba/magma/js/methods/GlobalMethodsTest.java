/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.methods;

import org.junit.Test;
import org.mozilla.javascript.NativeArray;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mozilla.javascript.Context.getCurrentContext;

public class GlobalMethodsTest extends AbstractJsTest {

//  private static final Logger log = LoggerFactory.getLogger(GlobalMethodsTest.class);

  @Test
  public void test_newValue_inferred_int() throws Exception {
    ScriptableValue sv = GlobalMethods.newValue(getCurrentContext(), getSharedScope(), new Object[] { 1 }, null);
    assertThat(sv.getValue().isNull()).isFalse();
    assertThat(sv.getValue().isSequence()).isFalse();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat((Long) sv.getValue().getValue()).isEqualTo(1l);
  }

  @Test
  public void test_newValue_int() throws Exception {
    ScriptableValue sv = GlobalMethods
        .newValue(getCurrentContext(), getSharedScope(), new Object[] { "1", "integer" }, null);
    assertThat(sv.getValue().isNull()).isFalse();
    assertThat(sv.getValue().isSequence()).isFalse();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat((Long) sv.getValue().getValue()).isEqualTo(1l);
  }

  @Test
  public void test_newValue_null_int() throws Exception {
    ScriptableValue sv = GlobalMethods
        .newValue(getCurrentContext(), getSharedScope(), new Object[] { null, "integer" }, null);
    assertThat(sv.getValue().isNull()).isTrue();
    assertThat(sv.getValue().isSequence()).isFalse();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_newValue_wrong_type() throws Exception {
    GlobalMethods.newValue(getCurrentContext(), getSharedScope(), new Object[] { "qwerty", "integer" }, null);
  }

  @Test
  public void test_newSequence_int() throws Exception {
    ScriptableValue sv = GlobalMethods
        .newSequence(getCurrentContext(), getSharedScope(), new Object[] { new NativeArray(new Object[] { 1, 2, 3 }) },
            null);
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat(sv.getValue().asSequence().getSize()).isEqualTo(3);
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((IntegerType) value.getValueType()).isEqualTo(IntegerType.get());
      assertThat((Long) value.getValue()).isEqualTo((long) i);
    }
  }

  @Test
  public void test_newSequence_String() throws Exception {
    ScriptableValue sv = GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] { "1", "2", "3" }) }, null);
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((TextType) sv.getValueType()).isEqualTo(TextType.get());
    assertThat(sv.getValue().asSequence().getSize()).isEqualTo(3);
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((TextType) value.getValueType()).isEqualTo(TextType.get());
      assertThat((String) value.getValue()).isEqualTo(String.valueOf(i));
    }
  }

  @Test
  public void test_newSequence_with_int_type() throws Exception {
    ScriptableValue sv = GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] { "1", "2", "3" }), "integer" }, null);
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat(sv.getValue().asSequence().getSize()).isEqualTo(3);
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((IntegerType) value.getValueType()).isEqualTo(IntegerType.get());
      assertThat((Long) value.getValue()).isEqualTo((long) i);
    }
  }

  @Test
  public void test_newSequence_String_with_empty_array() throws Exception {
    ScriptableValue sv = GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] {}), "text" }, null);
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((TextType) sv.getValueType()).isEqualTo(TextType.get());
    assertThat(sv.getValue().asSequence().getSize()).isEqualTo(0);
  }

  @Test
  public void test_newSequence_String_with_null() throws Exception {
    ScriptableValue sv = GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] { "1", "2", "3", null }) }, null);
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((TextType) sv.getValueType()).isEqualTo(TextType.get());
    assertThat(sv.getValue().asSequence().getSize()).isEqualTo(4);
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((TextType) value.getValueType()).isEqualTo(TextType.get());
      assertThat((String) value.getValue()).isEqualTo(String.valueOf(i));
    }
    assertThat((TextType) sequence.get(3).getValueType()).isEqualTo(TextType.get());
    assertThat(sequence.get(3).isNull()).isTrue();
  }

  @Test
  public void test_newSequence_String_with_null_value_only() throws Exception {
    ScriptableValue sv = GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] {
          GlobalMethods.newValue(getCurrentContext(), getSharedScope(), new Object[] { null, "text" }, null) }) }, null);
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((TextType) sv.getValueType()).isEqualTo(TextType.get());
    assertThat(sv.getValue().asSequence().getSize()).isEqualTo(1);
    ValueSequence sequence = sv.getValue().asSequence();
    assertThat((TextType) sequence.get(0).getValueType()).isEqualTo(TextType.get());
    assertThat(sequence.get(0).isNull()).isTrue();
  }

  @Test
  public void test_newSequence_String_with_null_only() throws Exception {
    ScriptableValue sv = GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] { null }), "text" }, null);
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((TextType) sv.getValueType()).isEqualTo(TextType.get());
    assertThat(sv.getValue().asSequence().getSize()).isEqualTo(1);
    ValueSequence sequence = sv.getValue().asSequence();
    assertThat((TextType) sequence.get(0).getValueType()).isEqualTo(TextType.get());
    assertThat(sequence.get(0).isNull()).isTrue();
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_newSequence_String_with_null_only_and_no_value_type() throws Exception {
    GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] { null }) }, null);
  }

  @Test
  public void test_newSequence_String_with_null_and_value_type() throws Exception {
    ScriptableValue sv = GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] { "1", "2", "3", null }), "text" }, null);
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((TextType) sv.getValueType()).isEqualTo(TextType.get());
    assertThat(sv.getValue().asSequence().getSize()).isEqualTo(4);
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((TextType) value.getValueType()).isEqualTo(TextType.get());
      assertThat((String) value.getValue()).isEqualTo(String.valueOf(i));
    }
    assertThat((TextType) sequence.get(3).getValueType()).isEqualTo(TextType.get());
    assertThat(sequence.get(3).isNull()).isTrue();
  }
}
