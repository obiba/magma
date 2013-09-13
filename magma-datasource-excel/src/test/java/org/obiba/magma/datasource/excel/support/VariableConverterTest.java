package org.obiba.magma.datasource.excel.support;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class VariableConverterTest {

  @Test
  public void testFindNormalizedHeader() {
    Assert.assertEquals("Value Type",
        ExcelUtil.findNormalizedHeader(Arrays.asList("Name", "Value Type"), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("Value_Type",
        ExcelUtil.findNormalizedHeader(Arrays.asList("Name", "Value_Type"), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("Value-Type",
        ExcelUtil.findNormalizedHeader(Arrays.asList("Name", "Value-Type"), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("value type",
        ExcelUtil.findNormalizedHeader(Arrays.asList("Name", "value type"), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("value_type",
        ExcelUtil.findNormalizedHeader(Arrays.asList("Name", "value_type"), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("value-type",
        ExcelUtil.findNormalizedHeader(Arrays.asList("Name", "value-type"), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("valuetype",
        ExcelUtil.findNormalizedHeader(Arrays.asList("Name", "valuetype"), VariableConverter.VALUE_TYPE));
    Assert.assertEquals("VALUE_TYPE",
        ExcelUtil.findNormalizedHeader(Arrays.asList("Name", "VALUE_TYPE"), VariableConverter.VALUE_TYPE));
    Assert.assertNull(ExcelUtil.findNormalizedHeader(Arrays.asList("Name", "Data Type"), VariableConverter.VALUE_TYPE));
  }

}
