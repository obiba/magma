package org.obiba.magma.filter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;

public class VariableValueTypeFilterTest {

  private Variable textVariable;

  private Variable binaryVariable;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
    textVariable = Variable.Builder.newVariable("Admin.Participant.Name", TextType.get(), "Participant").build();
    binaryVariable = Variable.Builder.newVariable("Binary", BinaryType.get(), "Participant").build();
  }

  @After
  public void cleanUp() throws Exception {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_filter_filtersType() {
    VariableValueTypeFilter filter = new VariableValueTypeFilter("text");
    assertThat(filter.runFilter(textVariable)).isTrue();
    assertThat(filter.runFilter(binaryVariable)).isFalse();
  }

  @Test
  public void test_filter_ignoresCase() {
    VariableValueTypeFilter filter = new VariableValueTypeFilter("TEXT");
    assertThat(filter.runFilter(textVariable)).isTrue();
    assertThat(filter.runFilter(binaryVariable)).isFalse();
  }

}
