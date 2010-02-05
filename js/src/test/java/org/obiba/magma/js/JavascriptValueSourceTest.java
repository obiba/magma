package org.obiba.magma.js;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.mozilla.javascript.EvaluatorException;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;

public class JavascriptValueSourceTest extends AbstractJsTest {

  @Test
  public void testSimpleScript() {
    JavascriptValueSource source = new JavascriptValueSource();
    source.setValueType(DecimalType.get());
    source.setScript("1;");

    source.initialise();

    Value value = source.getValue(null);
    Assert.assertEquals(new Double(1), value.getValue());
  }

  @Test
  public void testEngineMethod() {
    JavascriptValueSource source = new JavascriptValueSource();
    source.setValueType(IntegerType.get());
    source.setScript("now()");
    source.initialise();

    Value value = source.getValue(null);
    Assert.assertNotNull(value);
    Assert.assertFalse(value.isNull());
    Assert.assertEquals(DateTimeType.get(), value.getValueType());

    Date dateValue = (Date) value.getValue();
    Date now = new Date();
    // Make sure both dates are within 1 second of one-another
    Assert.assertTrue((now.getTime() - dateValue.getTime()) < 1000);
  }

  @Test
  public void testCompileError() {
    JavascriptValueSource source = new JavascriptValueSource();
    source.setValueType(IntegerType.get());
    // Error is on second line of script
    source.setScript("var i = 1+1;\nERROR!");
    source.setScriptName("Bogus");
    try {
      source.initialise();
      Assert.assertTrue("Exception was expected", false);
    } catch(EvaluatorException e) {
      Assert.assertEquals("Bogus", e.sourceName());
      Assert.assertEquals(2, e.lineNumber());
      Assert.assertEquals("ERROR!", e.lineSource());
    } catch(RuntimeException e) {
      Assert.assertTrue("Unexpected exception type " + e.getClass(), false);
    }
  }
}
