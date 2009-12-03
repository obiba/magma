package org.obiba.magma.js.methods;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.js.AbstractScriptableValueTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.TextType;

public class TextMethodsTest extends AbstractScriptableValueTest {

  @Test
  public void testTrim() {
    ScriptableValue value = newValue(TextType.get().valueOf(" Value  "));
    ScriptableValue result = TextMethods.trim(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(TextType.get().valueOf("Value"), result.getValue());
  }

  @Test
  public void testReplace() {
    ScriptableValue value = newValue(TextType.get().valueOf("H2R 2E1"));
    ScriptableValue result = TextMethods.replace(Context.getCurrentContext(), value, new Object[] { " 2E1", "" }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(TextType.get().valueOf("H2R"), result.getValue());
  }
}
