package org.obiba.meta.js.methods;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.meta.js.AbstractScriptableValueTest;
import org.obiba.meta.js.ScriptableValue;
import org.obiba.meta.type.BooleanType;
import org.obiba.meta.type.TextType;

public class BooleanMethodsTest extends AbstractScriptableValueTest {

  @Test
  public void testAny() {
    ScriptableValue value = newValue(TextType.get().valueOf("CAT2"));
    ScriptableValue result = BooleanMethods.any(Context.getCurrentContext(), value, new String[] { "CAT1", "CAT2" }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().trueValue(), result.getSingleValue());
  }

  @Test
  public void testAnyWithNullValue() {
    ScriptableValue value = newValue(TextType.get().valueOf(null));
    ScriptableValue result = BooleanMethods.any(Context.getCurrentContext(), value, new String[] { "CAT1", "CAT2" }, null);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.getSingleValue().isNull());
  }

  @Test
  public void testNot() {
    ScriptableValue value = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods.not(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().falseValue(), result.getSingleValue());
  }

}
