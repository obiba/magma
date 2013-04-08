package org.obiba.magma.xstream.converter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableList;
import com.thoughtworks.xstream.XStream;

public class ValueSequenceConverterTest {

  XStream xstream;

  @Before
  public void startYourEngine() {
    new MagmaEngine();
    xstream = new XStream();
    xstream.registerConverter(new ValueConverter());
    xstream.registerConverter(new ValueSequenceConverter());
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testValueSequenceWithNotNullValues() {
    ValueType type = TextType.get();
    Value value = type.sequenceOf(ImmutableList.of(type.valueOf("First Value"), type.valueOf("Second Value")));

    String xml = xstream.toXML(value);
    Value unmarshalled = (Value) xstream.fromXML(xml);

    Assert.assertEquals(value, unmarshalled);
    Assert.assertTrue(unmarshalled.isSequence());
    Assert.assertEquals(2, unmarshalled.asSequence().getValues().size());
  }

  @Test
  public void testNullValueSequence() {
    Value value = TextType.get().nullSequence();

    String xml = xstream.toXML(value);
    Value unmarshalled = (Value) xstream.fromXML(xml);

    Assert.assertEquals(value, unmarshalled);
    Assert.assertTrue(unmarshalled.isNull());

  }

}
