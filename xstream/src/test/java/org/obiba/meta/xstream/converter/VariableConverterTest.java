package org.obiba.meta.xstream.converter;

import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Variable;
import org.obiba.meta.type.TextType;

import com.thoughtworks.xstream.XStream;

public class VariableConverterTest {

  @Before
  public void startYourEngine() {
    new MetaEngine();
  }

  @After
  public void stopYourEngine() {
    MetaEngine.get().shutdown();
  }

  @Test
  public void testBasicVariable() {
    XStream xstream = new XStream();
    xstream.registerConverter(new VariableConverter(xstream.getMapper()));

    Variable v = newVariable().build();
    String xml = xstream.toXML(v);
    Variable unmarshalled = (Variable) xstream.fromXML(xml);

    Assert.assertEquals(v.getCollection(), unmarshalled.getCollection());
    Assert.assertEquals(v.getName(), unmarshalled.getName());
    Assert.assertEquals(v.getValueType(), unmarshalled.getValueType());
    Assert.assertEquals(v.getEntityType(), unmarshalled.getEntityType());
  }

  @Test
  public void testVariableWithAttributes() {
    XStream xstream = new XStream();
    xstream.registerConverter(new VariableConverter(xstream.getMapper()));
    xstream.registerConverter(new AttributeConverter());

    Variable v = newVariable().addAttribute("firstAttribute", "firstValue").addAttribute("secondAttribute", "secondValue", Locale.ENGLISH).build();
    String xml = xstream.toXML(v);
    Variable unmarshalled = (Variable) xstream.fromXML(xml);
    Assert.assertTrue(unmarshalled.hasAttribute("firstAttribute"));
    Assert.assertTrue(unmarshalled.hasAttribute("secondAttribute"));
    Assert.assertTrue(unmarshalled.getAttribute("secondAttribute").isLocalised());
  }

  protected Variable.Builder newVariable() {
    return Variable.Builder.newVariable("my-collection", "Test.Variable", TextType.get(), "Participant");
  }

}
