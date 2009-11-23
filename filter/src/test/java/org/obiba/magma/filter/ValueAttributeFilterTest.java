package org.obiba.magma.filter;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;

public class ValueAttributeFilterTest {

  private static final String TEST_ATTRIBUTE_NAME = "TEST_ATTRIBUTE_NAME";

  private static final String TEST_ATTRIBUTE_VALUE = "TEST_ATTRIBUTE_VALUE";

  private Variable variable;

  private VariableValueSource variableValueSourceMock;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
    variable = Variable.Builder.newVariable("collectionName", "Admin.Participant.Name", ValueType.Factory.forName("text"), "Participant").addAttribute(TEST_ATTRIBUTE_NAME, TEST_ATTRIBUTE_VALUE).build();
    variableValueSourceMock = EasyMock.createMock(VariableValueSource.class);
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
    VariableAttributeFilter filter = VariableAttributeFilter.Builder.newFilter().attributeName("attributeDoesNotExist").attributeValue(TEST_ATTRIBUTE_VALUE).include().build();
    expect(variableValueSourceMock.getVariable()).andReturn(variable);
    replay(variableValueSourceMock);
    assertThat(filter.runFilter(variableValueSourceMock), is(false));
    verify(variableValueSourceMock);
  }

  @Test
  public void testValueNotThereReturnsFalse() throws Exception {
    VariableAttributeFilter filter = VariableAttributeFilter.Builder.newFilter().attributeName(TEST_ATTRIBUTE_NAME).attributeValue("valueDoesNotExist").include().build();
    expect(variableValueSourceMock.getVariable()).andReturn(variable);
    replay(variableValueSourceMock);
    assertThat(filter.runFilter(variableValueSourceMock), is(false));
    verify(variableValueSourceMock);
  }

  @Test
  public void testAttributeAndValueFoundReturnsTrue() throws Exception {
    VariableAttributeFilter filter = VariableAttributeFilter.Builder.newFilter().attributeName(TEST_ATTRIBUTE_NAME).attributeValue(TEST_ATTRIBUTE_VALUE).include().build();
    expect(variableValueSourceMock.getVariable()).andReturn(variable);
    replay(variableValueSourceMock);
    assertThat(filter.runFilter(variableValueSourceMock), is(true));
    verify(variableValueSourceMock);
  }

  @Test
  public void testSuccessfulExcludeFilterLeavesItemInOutState() throws Exception {
    VariableAttributeFilter filter = VariableAttributeFilter.Builder.newFilter().attributeName(TEST_ATTRIBUTE_NAME).attributeValue(TEST_ATTRIBUTE_VALUE).exclude().build();
    StateEnvelope<VariableValueSource> stateEnvelope = new StateEnvelope<VariableValueSource>(variableValueSourceMock);
    stateEnvelope.setState(FilterState.IN);
    expect(variableValueSourceMock.getVariable()).andReturn(variable);
    replay(variableValueSourceMock);
    assertThat(filter.doIt(stateEnvelope).getState(), is(FilterState.OUT));
    verify(variableValueSourceMock);
  }

  @Test
  public void testSuccessfulIncludeFilterLeavesItemInInState() throws Exception {
    VariableAttributeFilter filter = VariableAttributeFilter.Builder.newFilter().attributeName(TEST_ATTRIBUTE_NAME).attributeValue(TEST_ATTRIBUTE_VALUE).include().build();
    StateEnvelope<VariableValueSource> stateEnvelope = new StateEnvelope<VariableValueSource>(variableValueSourceMock);
    stateEnvelope.setState(FilterState.OUT);
    expect(variableValueSourceMock.getVariable()).andReturn(variable);
    replay(variableValueSourceMock);
    assertThat(filter.doIt(stateEnvelope).getState(), is(FilterState.IN));
    verify(variableValueSourceMock);
  }

}
