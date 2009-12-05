package org.obiba.magma.xstream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;
import org.obiba.magma.xstream.XStreamValueSet;

import com.thoughtworks.xstream.XStream;

public class XStreamValueSetTest extends AbstractMagmaXStreamTest {

  private Variable testVariable;

  @Before
  public void initialise() {
    testVariable = Variable.Builder.newVariable("testTable", "TestVar", TextType.get(), "TestType").build();
  }

  @Test
  public void testEmptyValueSet() {
    XStreamValueSet valueSet = new XStreamValueSet("testTable", new VariableEntityBean("TestType", "id"));
    Assert.assertTrue(valueSet.getValue(testVariable).isNull());
  }

  @Test
  public void testGetValueReturnsAddedValue() {
    XStreamValueSet valueSet = new XStreamValueSet("testTable", new VariableEntityBean("TestType", "id"));

    Value testValue = TextType.get().valueOf("TestValue");
    valueSet.setValue(testVariable, testValue);

    Value value = valueSet.getValue(testVariable);
    Assert.assertEquals(testValue, value);
  }

  @Test
  public void testUnmarshall() {
    XStreamValueSet valueSet = new XStreamValueSet("testTable", new VariableEntityBean("TestType", "id"));

    Value testValue = TextType.get().valueOf("TestValue");
    valueSet.setValue(testVariable, testValue);

    XStream xstream = getDefaultXStream();

    String xml = xstream.toXML(valueSet);
    XStreamValueSet unmarshalled = (XStreamValueSet) xstream.fromXML(xml);

    Value value = unmarshalled.getValue(testVariable);
    Assert.assertEquals(testValue, value);
  }
}
