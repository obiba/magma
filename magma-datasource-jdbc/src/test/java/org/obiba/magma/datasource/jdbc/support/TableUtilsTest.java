package org.obiba.magma.datasource.jdbc.support;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class TableUtilsTest {

  @Test
  public void testNormalizeChars() {
    assertThat(TableUtils.normalize("'a 1/A.B$4")).isEqualTo("a1AB$4");
  }

  @Test
  public void testNormalizeLength() {
    assertThat(TableUtils.normalize("BLOOD_PRESSURE_MEASURE_NOTSAFE_REASON.OTHER_CONDITION.OTHER_CONDITION", 64)).isEqualTo("BLOOD_PRESSURE_MEASURE_NOTSAFE_REASONOTHER_CONDITIONOTHER_CONDI");
  }
}
