package org.obiba.meta.js;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Value;
import org.obiba.meta.type.DecimalType;
import org.obiba.meta.type.IntegerType;

public class JavascriptValueSourceTest {

  @Before
  public void startYourEngine() {
    new MetaEngine();
  }

  @After
  public void stopYourEngine() {
    MetaEngine.get().shutdown();
  }

  @Test
  public void testSimpleScript() {
    JavascriptValueSource source = new JavascriptValueSource();
    source.initialise();
    source.setValueType(DecimalType.get());
    source.setScript("1;");
    Value value = source.getValue(null);
    Assert.assertEquals(new Double(1), value.getValue());

  }

  @Test
  public void testEngineMethod() {
    JavascriptValueSource source = new JavascriptValueSource();
    source.initialise();
    source.setValueType(IntegerType.get());
    source.setScript("dateYear(now())");
    Value value = source.getValue(null);
    Assert.assertEquals(new Integer(2009), value.getValue());

  }

}
