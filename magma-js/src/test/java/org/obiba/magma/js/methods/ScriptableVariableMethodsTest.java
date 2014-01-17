package org.obiba.magma.js.methods;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.AbstractJsTest;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

public class ScriptableVariableMethodsTest extends AbstractJsTest {

  ValueSet mockValueSet;

  @Before
  public void setup() {
    mockValueSet = createMock(ValueSet.class);
    expect(mockValueSet.getValueTable()).andReturn(createMock(ValueTable.class)).anyTimes();
    expect(mockValueSet.getVariableEntity()).andReturn(createMock(VariableEntity.class)).anyTimes();
    replay(mockValueSet);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDefineMethodsForNull() {
    ScriptableVariableMethods.defineMethods(null);
  }

  @Test
  public void defineMethodsForDefinedness() {
    ScriptableObject prototype = new NativeObject();
    ScriptableObject actual = ScriptableVariableMethods.defineMethods(prototype);
    prototype
        .defineFunctionProperties(new String[] { "name", "attribute", "repeatable" }, ScriptableVariableMethods.class,
            ScriptableObject.DONTENUM);
    assertThat(actual).isEqualTo(prototype);
  }

  @Test(expected = IllegalArgumentException.class)
  public void repeatableWithNullValue() {
    Object value = evaluate("repeatable()", (Variable) null);
    assertThat(value).isNull();
  }

  @Test
  public void repeatableWithMockValue() {
    Variable mockVariable = createMock(Variable.class);
    expect(mockVariable.isRepeatable()).andReturn(true).once();
    replay(mockVariable);
    evaluate("repeatable()", mockVariable);
    verify(mockVariable);

  }

  @Test
  public void occurrenceGroupWithMockValue() {
    Variable mockVariable = createMock(Variable.class);
    expect(mockVariable.getOccurrenceGroup()).andReturn("patate").once();
    replay(mockVariable);
    evaluate("occurrenceGroup()", mockVariable);
    verify(mockVariable);

  }

  @Test
  public void entityTypeWithMockValue() {
    Variable mockVariable = createMock(Variable.class);
    expect(mockVariable.getEntityType()).andReturn("Participant").once();
    replay(mockVariable);
    evaluate("entityType()", mockVariable);
    verify(mockVariable);
  }

  @Test
  public void mimeTypeWithMockValue() {
    Variable mockVariable = createMock(Variable.class);
    expect(mockVariable.getMimeType()).andReturn("application/octet-stream").once();
    replay(mockVariable);
    evaluate("mimeType()", mockVariable);
    verify(mockVariable);
  }

  @Test
  public void unitWithMockValue() {
    Variable mockVariable = createMock(Variable.class);
    expect(mockVariable.getUnit()).andReturn("kg").once();
    replay(mockVariable);
    evaluate("unit()", mockVariable);
    verify(mockVariable);
  }
}
