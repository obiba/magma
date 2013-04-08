package org.obiba.magma.js;

import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.type.TextType;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class JavascriptClauseTest extends AbstractJsTest {

  @Test
  public void testSelectWithScriptThatEvaluatesToTrue() {
    Variable variableMock = createMock(Variable.class);

    expect(variableMock.getName()).andReturn("Participant.DO_YOU_SMOKE").anyTimes();
    replay(variableMock);

    JavascriptClause javascriptClause = new JavascriptClause("name().matches(/Participant.*/, /DO_YOU_SMOKE/)");
    javascriptClause.initialise();
    boolean result = javascriptClause.select(variableMock);

    // Verify behaviour.
    verify(variableMock);

    // Verify state.
    assertEquals(true, result);
  }

  @Test
  public void testSelectWithScriptThatEvaluatesToFalse() {
    Variable variableMock = createMock(Variable.class);

    expect(variableMock.getName()).andReturn("Participant.DO_YOU_SMOKE").anyTimes();
    replay(variableMock);

    JavascriptClause javascriptClause = new JavascriptClause("name().matches(/SomeUnmatchedPattern/)");
    javascriptClause.initialise();
    boolean result = javascriptClause.select(variableMock);

    // Verify behaviour.
    verify(variableMock);

    // Verify state.
    assertEquals(false, result);
  }

  @Test
  public void testWhereWithScriptThatEvaluatesToTrue() {
    ValueTable valueTableMock = createMock(ValueTable.class);
    ValueSet valueSetMock = createMock(ValueSet.class);
    VariableValueSource variableValueSourceMock = createMock(VariableValueSource.class);
    Variable variableMock = createMock(Variable.class);

    expect(valueSetMock.getValueTable()).andReturn(valueTableMock).anyTimes();
    expect(valueTableMock.getVariableValueSource("DO_YOU_SMOKE")).andReturn(variableValueSourceMock).anyTimes();
    expect(variableValueSourceMock.getValue(valueSetMock)).andReturn(TextType.get().valueOf("Yes"));
    expect(variableValueSourceMock.getVariable()).andReturn(variableMock).once();
    expect(variableMock.getUnit()).andReturn(null).once();

    replay(valueSetMock, valueTableMock, variableValueSourceMock, variableMock);

    JavascriptClause javascriptClause = new JavascriptClause("$('DO_YOU_SMOKE').any('DNK', 'PNA').not()");
    javascriptClause.initialise();
    boolean result = javascriptClause.where(valueSetMock);

    // Verify behaviour.
    verify(valueSetMock, valueTableMock, variableValueSourceMock, variableMock);

    // Verify state.
    assertEquals(true, result);
  }

  @Test
  public void testWhereWithScriptThatEvaluatesToFalse() {
    ValueTable valueTableMock = createMock(ValueTable.class);
    ValueSet valueSetMock = createMock(ValueSet.class);
    VariableValueSource variableValueSourceMock = createMock(VariableValueSource.class);
    Variable variableMock = createMock(Variable.class);

    expect(valueSetMock.getValueTable()).andReturn(valueTableMock).anyTimes();
    expect(valueTableMock.getVariableValueSource("DO_YOU_SMOKE")).andReturn(variableValueSourceMock).anyTimes();
    expect(variableValueSourceMock.getValue(valueSetMock)).andReturn(TextType.get().valueOf("DNK"));
    expect(variableValueSourceMock.getVariable()).andReturn(variableMock).once();
    expect(variableMock.getUnit()).andReturn(null).once();
    replay(valueSetMock, valueTableMock, variableValueSourceMock, variableMock);

    JavascriptClause javascriptClause = new JavascriptClause("$('DO_YOU_SMOKE').any(true, 'DNK', 'PNA').not()");
    javascriptClause.initialise();
    boolean result = javascriptClause.where(valueSetMock);

    // Verify behaviour.
    verify(valueSetMock, valueTableMock, variableValueSourceMock, variableMock);

    // Verify state.
    assertEquals(false, result);
  }
}
