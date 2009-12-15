package org.obiba.magma.js.methods;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.js.AbstractScriptableValueTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateType;

public class DateTimeMethodTest extends AbstractScriptableValueTest {

  @Test
  public void testAfterNullArgumentReturnsNull() throws Exception {
    ScriptableValue now = newValue(DateType.get().valueOf(new Date()));
    ScriptableValue nullDate = newValue(DateType.get().nullValue());
    ScriptableValue result = (ScriptableValue) DateTimeMethods.after(Context.getCurrentContext(), now, new ScriptableValue[] { nullDate }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().nullValue(), result.getValue());
  }

  @Test
  public void testAfterNullCallerReturnsNull() throws Exception {
    ScriptableValue now = newValue(DateType.get().valueOf(new Date()));
    ScriptableValue nullDate = newValue(DateType.get().nullValue());
    ScriptableValue result = (ScriptableValue) DateTimeMethods.after(Context.getCurrentContext(), nullDate, new ScriptableValue[] { now }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().nullValue(), result.getValue());
  }
}
