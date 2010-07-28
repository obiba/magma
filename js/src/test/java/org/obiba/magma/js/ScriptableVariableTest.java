package org.obiba.magma.js;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;

public class ScriptableVariableTest extends AbstractJsTest {

  @Test
  public void testGetName() {
    Variable mockVariable = EasyMock.createMock(Variable.class);
    EasyMock.expect(mockVariable.getName()).andReturn("my-variable");
    EasyMock.replay(mockVariable);
    Object obj = evaluate("name()", mockVariable);
    Assert.assertTrue(obj instanceof ScriptableValue);
    Assert.assertEquals("my-variable", ((ScriptableValue) obj).getValue().getValue());
  }

  @Test
  public void testGetAttributeValue() {
    Variable mockVariable = EasyMock.createMock(Variable.class);
    EasyMock.expect(mockVariable.hasAttribute("an-attribute")).andReturn(true);
    EasyMock.expect(mockVariable.getAttributeValue("an-attribute")).andReturn(BooleanType.get().trueValue());
    EasyMock.replay(mockVariable);
    Object obj = evaluate("attribute('an-attribute')", mockVariable);
    Assert.assertTrue(obj instanceof ScriptableValue);
    Assert.assertEquals(BooleanType.get().trueValue(), ((ScriptableValue) obj).getValue());
  }

}
