package org.obiba.magma.js;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.EvaluatorException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import junit.framework.Assert;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class JavascriptValueSourceTest extends AbstractJsTest {

  ValueSet mockValueSet;

  @Before
  public void setup() {
    mockValueSet = createMock(ValueSet.class);
    expect(mockValueSet.getValueTable()).andReturn(createMock(ValueTable.class)).anyTimes();
    expect(mockValueSet.getVariableEntity()).andReturn(createMock(VariableEntity.class)).anyTimes();
    replay(mockValueSet);
  }

  @Test
  public void testSimpleScript() {
    JavascriptValueSource source = new JavascriptValueSource(DecimalType.get(), "1");
    source.initialise();

    Value value = source.getValue(mockValueSet);
    Assert.assertEquals(new Double(1), value.getValue());
  }

  @Test
  public void testEngineMethod() {
    JavascriptValueSource source = new JavascriptValueSource(DateTimeType.get(), "now()");
    source.initialise();

    Value value = source.getValue(mockValueSet);
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
    // Error is on second line of script
    String script = "var i = 1+1;\nERROR!";
    JavascriptValueSource source = new JavascriptValueSource(IntegerType.get(), script);
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

  @Test
  public void test_Opal1110() {
    JavascriptValueSource source = new JavascriptValueSource(TextType.get(), "1");
    source.initialise();

    Value value = source.getValue(mockValueSet);
    Assert.assertEquals("1", value.getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testInitiliseWithNullValue() {
    JavascriptValueSource source = null;
    source.initialise();
  }
}
