package org.obiba.magma.datasource.excel.support;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obiba.magma.datasource.excel.support.ExcelUtil.findNormalizedHeader;
import static org.obiba.magma.datasource.excel.support.VariableConverter.VALUE_TYPE;

public class VariableConverterTest {

  @Test
  public void testFindNormalizedHeader() {
    assertThat(findNormalizedHeader(asList("Name", "Value Type"), VALUE_TYPE)).isEqualTo("Value Type");
    assertThat(findNormalizedHeader(asList("Name", "Value_Type"), VALUE_TYPE)).isEqualTo("Value_Type");
    assertThat(findNormalizedHeader(asList("Name", "Value-Type"), VALUE_TYPE)).isEqualTo("Value-Type");
    assertThat(findNormalizedHeader(asList("Name", "value type"), VALUE_TYPE)).isEqualTo("value type");
    assertThat(findNormalizedHeader(asList("Name", "value_type"), VALUE_TYPE)).isEqualTo("value_type");
    assertThat(findNormalizedHeader(asList("Name", "value-type"), VALUE_TYPE)).isEqualTo("value-type");
    assertThat(findNormalizedHeader(asList("Name", "valuetype"), VALUE_TYPE)).isEqualTo("valuetype");
    assertThat(findNormalizedHeader(asList("Name", "VALUE_TYPE"), VALUE_TYPE)).isEqualTo("VALUE_TYPE");
    assertThat(findNormalizedHeader(asList("Name", "Data Type"), VALUE_TYPE)).isNull();
  }

}
