package org.obiba.magma.js.methods;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GlobalMethodsTest extends AbstractJsTest {

  @Test
  public void test_newSequence_int() throws Exception {
    ScriptableValue seq = GlobalMethods.newSequence(Context.getCurrentContext(), getMagmaContext().sharedScope(),
        new Object[] { new NativeArray(new Object[] { 1, 2, 3 }) }, null);
    assertThat(seq.getValue().isSequence(), is(true));
    assertThat((IntegerType) seq.getValueType(), is(IntegerType.get()));
    assertThat(seq.getValue().getLength(), is(3l));
  }

  @Test
  public void test_newSequence_String() throws Exception {
    ScriptableValue seq = GlobalMethods.newSequence(Context.getCurrentContext(), getMagmaContext().sharedScope(),
        new Object[] { new NativeArray(new Object[] { "1", "2", "3" }) }, null);
    assertThat(seq.getValue().isSequence(), is(true));
    assertThat((TextType) seq.getValueType(), is(TextType.get()));
    assertThat(seq.getValue().getLength(), is(3l));
  }

  @Test
  public void test_newSequence_with_int_type() throws Exception {
    ScriptableValue seq = GlobalMethods.newSequence(Context.getCurrentContext(), getMagmaContext().sharedScope(),
        new Object[] { new NativeArray(new Object[] { "1", "2", "3" }), "integer" }, null);
    assertThat(seq.getValue().isSequence(), is(true));
    assertThat((IntegerType) seq.getValueType(), is(IntegerType.get()));
    assertThat(seq.getValue().getLength(), is(3l));
  }
}
