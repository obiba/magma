package org.obiba.magma.filter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VariableNameFilterTest {

  private Variable variable;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
    variable = Variable.Builder.newVariable("Admin.Participant.Name", ValueType.Factory.forName("text"), "Participant")
        .build();
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
    assertThat(filter.runFilter(variable), is(true));
  }

  @Test
  public void testPrefixFailure() throws Exception {
    VariableNameFilter filter = new VariableNameFilter("NothingToMatchHere", null);
    assertThat(filter.runFilter(variable), is(false));
  }

  @Test
  public void testRegexMatchSuccessOne() throws Exception {
    VariableNameFilter filter = new VariableNameFilter(null, "Admin.*Name");
    assertThat(filter.runFilter(variable), is(true));
  }

  @Test
  public void testRegexMatchSuccessTwo() throws Exception {
    VariableNameFilter filter = new VariableNameFilter(null, "^.dmin.*N.*$");
    assertThat(filter.runFilter(variable), is(true));
  }

  @Test
  public void testRegexMatchFailure() throws Exception {
    VariableNameFilter filter = new VariableNameFilter(null, "No.*Match");
    assertThat(filter.runFilter(variable), is(false));
  }

}
