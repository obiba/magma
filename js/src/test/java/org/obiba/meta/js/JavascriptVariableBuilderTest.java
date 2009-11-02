package org.obiba.meta.js;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.meta.Attribute;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Variable;
import org.obiba.meta.type.BooleanType;

public class JavascriptVariableBuilderTest {

  @Before
  public void startYourEngine() {
    new MetaEngine();
  }

  @After
  public void stopYourEngine() {
    MetaEngine.get().shutdown();
  }

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
