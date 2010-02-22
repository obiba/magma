package org.obiba.magma.js;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
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

  protected Object evaluate(final String script, final Variable variable) {
    return ContextFactory.getGlobal().call(new ContextAction() {
      public Object run(Context ctx) {
        MagmaContext context = MagmaContext.asMagmaContext(ctx);
        // Don't pollute the global scope
        Scriptable scope = new ScriptableVariable(context.newLocalScope(), variable);

        final Script compiledScript = context.compileString(script, "", 1, null);
        Object value = compiledScript.exec(ctx, scope);

        return value;
      }
    });
  }
}
