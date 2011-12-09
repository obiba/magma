package org.obiba.magma.js.methods;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.AbstractJsTest;

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
  public void testDefineMethodsFornull() {
    ScriptableObject prototype = new NativeObject();
    prototype = null;
    ScriptableVariableMethods.defineMethods(prototype);
  }

  @Test
  public void defineMethodsForDefinedness() {
    ScriptableObject prototype = new NativeObject();
    ScriptableObject actual = ScriptableVariableMethods.defineMethods(prototype);
    prototype.defineFunctionProperties(new String[] { "name", "attribute", "repeatable" }, ScriptableVariableMethods.class, ScriptableObject.DONTENUM);
    Assert.assertEquals(prototype, actual);
  }

  @Test(expected = IllegalArgumentException.class)
  public void repeatableWithNullValue() {
    Variable variable = null;
    Object value = evaluate("repeatable()", variable);
    Assert.assertEquals(value, null);
  }

  @Test
  public void repeatableWithMockValue() {
    Variable mockVariable = createMock(Variable.class);
    evaluate("repeatable()", mockVariable);
  }
}
