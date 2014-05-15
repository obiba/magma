package org.obiba.magma.filter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Attribute;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Variable;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;

public class ValueAttributeFilterTest {

  private static final String TEST_ATTRIBUTE_NAME = "TEST_ATTRIBUTE_NAME";

  private static final Integer TEST_ATTRIBUTE_VALUE = 42;

  private Variable variable;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
    variable = Variable.Builder.newVariable("Admin.Participant.Name", TextType.get(), "Participant").addAttribute(
        Attribute.Builder.newAttribute(TEST_ATTRIBUTE_NAME).withValue(IntegerType.get().valueOf(42)).build()).build();
  }

  @After
  public void cleanUp() throws Exception {
    MagmaEngine.get().shutdown();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoAttributeNoValueNotAllowed() throws Exception {
    new VariableAttributeFilter(null, null);
  }

  @Test
  public void testAttributeAndValueAllowed() throws Exception {
    new VariableAttributeFilter("attribute", "value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAttributeNoValueNotAllowed() throws Exception {
    new VariableAttributeFilter("attribute", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueNoAttributeNotAllowed() throws Exception {
    new VariableAttributeFilter(null, "value");
  }

  @Test
  public void testAttributeNotThereReturnsFalse() throws Exception {
    VariableAttributeFilter filter = VariableAttributeFilter.Builder.newFilter().attributeName("attributeDoesNotExist")
        .attributeValue(TEST_ATTRIBUTE_VALUE.toString()).include().build();
    assertThat(filter.runFilter(variable)).isFalse();
  }

  @Test
  public void testValueNotThereReturnsFalse() throws Exception {
    VariableAttributeFilter filter = VariableAttributeFilter.Builder.newFilter().attributeName(TEST_ATTRIBUTE_NAME)
        .attributeValue("valueDoesNotExist").include().build();
    assertThat(filter.runFilter(variable)).isFalse();
  }

  @Test
  public void testAttributeAndValueFoundReturnsTrue() throws Exception {
    VariableAttributeFilter filter = VariableAttributeFilter.Builder.newFilter().attributeName(TEST_ATTRIBUTE_NAME)
        .attributeValue(TEST_ATTRIBUTE_VALUE.toString()).include().build();
    assertThat(filter.runFilter(variable)).isTrue();
  }

  @Test
  public void testSuccessfulExcludeFilterLeavesItemInOutState() throws Exception {
    VariableAttributeFilter filter = VariableAttributeFilter.Builder.newFilter().attributeName(TEST_ATTRIBUTE_NAME)
        .attributeValue(TEST_ATTRIBUTE_VALUE.toString()).exclude().build();
    StateEnvelope<Variable> stateEnvelope = new StateEnvelope<>(variable);
    stateEnvelope.setState(FilterState.IN);
    assertThat(filter.doIt(stateEnvelope).getState()).isEqualTo(FilterState.OUT);
  }

  @Test
  public void testSuccessfulIncludeFilterLeavesItemInInState() throws Exception {
    VariableAttributeFilter filter = VariableAttributeFilter.Builder.newFilter().attributeName(TEST_ATTRIBUTE_NAME)
        .attributeValue(TEST_ATTRIBUTE_VALUE.toString()).include().build();
    StateEnvelope<Variable> stateEnvelope = new StateEnvelope<>(variable);
    stateEnvelope.setState(FilterState.OUT);
    assertThat(filter.doIt(stateEnvelope).getState()).isEqualTo(FilterState.IN);
  }

}
