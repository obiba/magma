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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class JavascriptValueSourceTest extends AbstractJsTest {

  private ValueSet mockValueSet;

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
    assertThat(value.getValue()).isEqualTo(1d);
  }

  @Test
  public void testEngineMethod() {
    JavascriptValueSource source = new JavascriptValueSource(DateTimeType.get(), "now()");
    source.initialise();

    Value value = source.getValue(mockValueSet);
    assertThat(value).isNotNull();
    assertThat(value.isNull()).isFalse();
    assertThat(value.getValueType()).isEqualTo(DateTimeType.get());

    Date dateValue = (Date) value.getValue();
    Date now = new Date();
    // Make sure both dates are within 1 second of one-another
    assertThat(now.getTime() - dateValue.getTime()).isLessThan(1000);
  }

  @Test
  public void testCompileError() {
    // Error is on second line of script
    String script = "var i = 1+1;\nERROR!";
    JavascriptValueSource source = new JavascriptValueSource(IntegerType.get(), script);
    source.setScriptName("Bogus");
    try {
      source.initialise();
      fail("Exception was expected");
    } catch(EvaluatorException e) {
      assertThat(e.sourceName()).isEqualTo("Bogus");
      assertThat(e.lineNumber()).isEqualTo(2);
      assertThat(e.lineSource()).isEqualTo("ERROR!");
    }
  }

  @Test
  public void test_Opal1110() {
    JavascriptValueSource source = new JavascriptValueSource(TextType.get(), "1");
    source.initialise();

    Value value = source.getValue(mockValueSet);
    assertThat(value.getValue()).isEqualTo("1");
  }

}
