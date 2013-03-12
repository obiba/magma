package org.obiba.magma.filter;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableValueSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExcludeAllFilterTest {

  private VariableValueSource variableValueSourceMock;

  private ValueSet valueSetMock;

  @Before
  public void setUp() throws Exception {
    variableValueSourceMock = EasyMock.createMock(VariableValueSource.class);
    valueSetMock = EasyMock.createMock(ValueSet.class);
  }

  @Test
  public void testValueSetFilterAlwaysReturnsTrue() throws Exception {
    ExcludeAllFilter<ValueSet> filter = ExcludeAllFilter.Builder.newFilter().buildForValueSet();
    assertThat(filter.runFilter(valueSetMock), is(true));
  }

  @Test
  public void testValueSetFilterIsAlwaysTheExcludeType() throws Exception {
    ExcludeAllFilter<ValueSet> filter = ExcludeAllFilter.Builder.newFilter().buildForValueSet();
    StateEnvelope<ValueSet> stateEnvelope = new StateEnvelope<ValueSet>(valueSetMock);
    assertThat(filter.doIt(stateEnvelope).getState(), is(FilterState.OUT));
  }

  @Test
  public void testVariableValueSourceFilterAlwaysReturnsTrue() throws Exception {
    ExcludeAllFilter<VariableValueSource> filter = ExcludeAllFilter.Builder.newFilter().buildForVariableValueSource();
    assertThat(filter.runFilter(variableValueSourceMock), is(true));
  }

  @Test
  public void testVariableValueSourceFilterIsAlwaysTheExcludeType() throws Exception {
    ExcludeAllFilter<VariableValueSource> filter = ExcludeAllFilter.Builder.newFilter().buildForVariableValueSource();
    StateEnvelope<VariableValueSource> stateEnvelope = new StateEnvelope<VariableValueSource>(variableValueSourceMock);
    assertThat(filter.doIt(stateEnvelope).getState(), is(FilterState.OUT));
  }
}
