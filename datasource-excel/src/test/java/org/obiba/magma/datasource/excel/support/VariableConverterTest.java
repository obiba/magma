package org.obiba.magma.datasource.excel.support;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public class VariableConverterTest {

  @Test
  public void testFindNormalizedHeader() {
    VariableConverter converter = new VariableConverter(null);

    Assert.assertEquals("Value Type", converter.findNormalizedHeader(Arrays.asList(new String[] { "Name", "Value Type" }), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("Value_Type", converter.findNormalizedHeader(Arrays.asList(new String[] { "Name", "Value_Type" }), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("Value-Type", converter.findNormalizedHeader(Arrays.asList(new String[] { "Name", "Value-Type" }), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("value type", converter.findNormalizedHeader(Arrays.asList(new String[] { "Name", "value type" }), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("value_type", converter.findNormalizedHeader(Arrays.asList(new String[] { "Name", "value_type" }), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("value-type", converter.findNormalizedHeader(Arrays.asList(new String[] { "Name", "value-type" }), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("valuetype", converter.findNormalizedHeader(Arrays.asList(new String[] { "Name", "valuetype" }), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("VALUE_TYPE", converter.findNormalizedHeader(Arrays.asList(new String[] { "Name", "VALUE_TYPE" }), VariableConverter.VALUE_TYPE));
    Assert.assertNull(converter.findNormalizedHeader(Arrays.asList(new String[] { "Name", "Data Type" }), VariableConverter.VALUE_TYPE));
  }

}
