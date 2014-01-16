package org.obiba.magma.filter;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableValueSource;

import static org.fest.assertions.api.Assertions.assertThat;

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
    assertThat(filter.runFilter(valueSetMock)).isTrue();
  }

  @Test
  public void testValueSetFilterIsAlwaysTheExcludeType() throws Exception {
    ExcludeAllFilter<ValueSet> filter = ExcludeAllFilter.Builder.newFilter().buildForValueSet();
    StateEnvelope<ValueSet> stateEnvelope = new StateEnvelope<>(valueSetMock);
    assertThat(filter.doIt(stateEnvelope).getState()).isEqualTo(FilterState.OUT);
  }

  @Test
  public void testVariableValueSourceFilterAlwaysReturnsTrue() throws Exception {
    ExcludeAllFilter<VariableValueSource> filter = ExcludeAllFilter.Builder.newFilter().buildForVariableValueSource();
    assertThat(filter.runFilter(variableValueSourceMock)).isTrue();
  }

  @Test
  public void testVariableValueSourceFilterIsAlwaysTheExcludeType() throws Exception {
    ExcludeAllFilter<VariableValueSource> filter = ExcludeAllFilter.Builder.newFilter().buildForVariableValueSource();
    StateEnvelope<VariableValueSource> stateEnvelope = new StateEnvelope<>(variableValueSourceMock);
    assertThat(filter.doIt(stateEnvelope).getState()).isEqualTo(FilterState.OUT);
  }
}
