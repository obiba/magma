package org.obiba.magma.js.validation;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.magma.Variable;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;

public class ScriptableVariableTest extends AbstractJsTest {

  @Test
  public void testGetName() {
    Variable mockVariable = EasyMock.createMock(Variable.class);
    EasyMock.expect(mockVariable.getName()).andReturn("my-variable");
    EasyMock.replay(mockVariable);
    Object obj = evaluate("name()", mockVariable);
    assertThat(obj).isInstanceOf(ScriptableValue.class);
    assertThat("my-variable").isEqualTo(((ScriptableValue) obj).getValue().getValue().toString());
  }

  @Test
  public void testGetAttributeValue() {
    Variable mockVariable = EasyMock.createMock(Variable.class);
    EasyMock.expect(mockVariable.hasAttribute("an-attribute")).andReturn(true);
    EasyMock.expect(mockVariable.getAttributeValue("an-attribute")).andReturn(BooleanType.get().trueValue());
    EasyMock.replay(mockVariable);
    Object obj = evaluate("attribute('an-attribute')", mockVariable);
    assertThat(obj).isInstanceOf(ScriptableValue.class);
    assertThat(((ScriptableValue) obj).getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_type_returnsValueTypeAsTextType() {
    Variable mockVariable = EasyMock.createMock(Variable.class);
    EasyMock.expect(mockVariable.getValueType()).andReturn(DecimalType.get());
    EasyMock.replay(mockVariable);
    Object obj = evaluate("type()", mockVariable);
    assertThat(obj).isInstanceOf(ScriptableValue.class);
    assertThat(((ScriptableValue) obj).getValue()).isEqualTo(TextType.get().valueOf(DecimalType.get().getName()));
  }
}
