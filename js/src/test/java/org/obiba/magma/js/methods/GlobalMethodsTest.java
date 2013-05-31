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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mozilla.javascript.Context.getCurrentContext;

public class GlobalMethodsTest extends AbstractJsTest {

  private static final Logger log = LoggerFactory.getLogger(GlobalMethodsTest.class);

  @Test
  public void test_newValue_inferred_int() throws Exception {
    ScriptableValue sv = GlobalMethods.newValue(getCurrentContext(), getSharedScope(), new Object[] { 1 }, null);
    assertThat(sv.getValue().isNull(), is(false));
    assertThat(sv.getValue().isSequence(), is(false));
    assertThat((IntegerType) sv.getValueType(), is(IntegerType.get()));
    assertThat((Long) sv.getValue().getValue(), is(1l));
  }

  @Test
  public void test_newValue_int() throws Exception {
    ScriptableValue sv = GlobalMethods
        .newValue(getCurrentContext(), getSharedScope(), new Object[] { "1", "integer" }, null);
    assertThat(sv.getValue().isNull(), is(false));
    assertThat(sv.getValue().isSequence(), is(false));
    assertThat((IntegerType) sv.getValueType(), is(IntegerType.get()));
    assertThat((Long) sv.getValue().getValue(), is(1l));
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
    assertThat(sv.getValue().isSequence(), is(true));
    assertThat((IntegerType) sv.getValueType(), is(IntegerType.get()));
    assertThat(sv.getValue().getLength(), is(3l));
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((IntegerType) value.getValueType(), is(IntegerType.get()));
      assertThat((Long) value.getValue(), is((long) i));
    }
  }

  @Test
  public void test_newSequence_String() throws Exception {
    ScriptableValue sv = GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] { "1", "2", "3" }) }, null);
    assertThat(sv.getValue().isSequence(), is(true));
    assertThat((TextType) sv.getValueType(), is(TextType.get()));
    assertThat(sv.getValue().getLength(), is(3l));
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((TextType) value.getValueType(), is(TextType.get()));
      assertThat((String) value.getValue(), is(String.valueOf(i)));
    }
  }

  @Test
  public void test_newSequence_with_int_type() throws Exception {
    ScriptableValue sv = GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] { "1", "2", "3" }), "integer" }, null);
    assertThat(sv.getValue().isSequence(), is(true));
    assertThat((IntegerType) sv.getValueType(), is(IntegerType.get()));
    assertThat(sv.getValue().getLength(), is(3l));
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((IntegerType) value.getValueType(), is(IntegerType.get()));
      assertThat((Long) value.getValue(), is((long) i));
    }
  }
}
