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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JavascriptValueSourceTest extends AbstractJsTest {

  private ValueSet mockValueSet;

  @Before
  public void setup() {
    mockValueSet = mock(ValueSet.class);
    when(mockValueSet.getValueTable()).thenReturn(mock(ValueTable.class));
    when(mockValueSet.getVariableEntity()).thenReturn(mock(VariableEntity.class));
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
    // Make sure both dates are within 1 second of one-another
    assertThat(System.currentTimeMillis() - dateValue.getTime()).isLessThan(1000);
  }

  @Test
  public void testCompileError() {
    // Error is on second line of script
    String script = "var i = 1+1;\nERROR!";
    JavascriptValueSource source = new JavascriptValueSource(IntegerType.get(), script);
    source.setScriptName("Bogus");
    try {
      source.initialise();
      fail("EvaluatorException was expected");
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
