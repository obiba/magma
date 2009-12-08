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

public class VariableNameFilterTest {

  private Variable variable;

  private VariableValueSource variableValueSourceMock;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
    variable = Variable.Builder.newVariable("Admin.Participant.Name", ValueType.Factory.forName("text"), "Participant").build();
    variableValueSourceMock = EasyMock.createMock(VariableValueSource.class);
  }

  @After
  public void cleanUp() throws Exception {
    MagmaEngine.get().shutdown();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoPrefixNoMatchNotAllowed() throws Exception {
    new VariableNameFilter(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrefixAndMatchNotAllowed() throws Exception {
    new VariableNameFilter("prefixValue", "matchValue");
  }

  @Test
  public void testPrefixNoMatchAllowed() throws Exception {
    new VariableNameFilter("prefixValue", null);
  }

  @Test
  public void testMatchNoPrefixAllowed() throws Exception {
    new VariableNameFilter(null, "matchValue");
  }

  @Test
  public void testPrefixSuccess() throws Exception {
    VariableNameFilter filter = new VariableNameFilter("Admin", null);
    expect(variableValueSourceMock.getVariable()).andReturn(variable);
    replay(variableValueSourceMock);
    assertThat(filter.runFilter(variableValueSourceMock), is(true));
    verify(variableValueSourceMock);
  }

  @Test
  public void testPrefixFailure() throws Exception {
    VariableNameFilter filter = new VariableNameFilter("NothingToMatchHere", null);
    expect(variableValueSourceMock.getVariable()).andReturn(variable);
    replay(variableValueSourceMock);
    assertThat(filter.runFilter(variableValueSourceMock), is(false));
    verify(variableValueSourceMock);
  }

  @Test
  public void testRegexMatchSuccessOne() throws Exception {
    VariableNameFilter filter = new VariableNameFilter(null, "Admin.*Name");
    expect(variableValueSourceMock.getVariable()).andReturn(variable);
    replay(variableValueSourceMock);
    assertThat(filter.runFilter(variableValueSourceMock), is(true));
    verify(variableValueSourceMock);
  }

  @Test
  public void testRegexMatchSuccessTwo() throws Exception {
    VariableNameFilter filter = new VariableNameFilter(null, "^.dmin.*N.*$");
    expect(variableValueSourceMock.getVariable()).andReturn(variable);
    replay(variableValueSourceMock);
    assertThat(filter.runFilter(variableValueSourceMock), is(true));
    verify(variableValueSourceMock);
  }

  @Test
  public void testRegexMatchFailure() throws Exception {
    VariableNameFilter filter = new VariableNameFilter(null, "No.*Match");
    expect(variableValueSourceMock.getVariable()).andReturn(variable);
    replay(variableValueSourceMock);
    assertThat(filter.runFilter(variableValueSourceMock), is(false));
    verify(variableValueSourceMock);
  }

}
