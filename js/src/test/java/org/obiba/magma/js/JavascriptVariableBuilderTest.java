package org.obiba.magma.js;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.magma.Attribute;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;

public class JavascriptVariableBuilderTest extends AbstractJsTest {

  @Test
  public void testSimpleVariable() {
    Variable.Builder builder = Variable.Builder.newVariable("myCollection", "myJsTest", BooleanType.get(), "Participant");
    try {
      Variable variable = builder.extend(JavascriptVariableBuilder.class).setScript("'Hello World!'").build();
      Attribute attribute = variable.getAttribute(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME);
      Assert.assertNotNull(attribute);
      Assert.assertNotNull(attribute.getValue());
      Assert.assertEquals("'Hello World!'", attribute.getValue().toString());
    } catch(RuntimeException e) {
      Assert.assertTrue("Unexpected exception: " + e.getMessage(), false);
    }
  }
}
