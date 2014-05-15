package org.obiba.magma.filter.views;

import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.filter.FilterChain;
import org.obiba.magma.views.WhereClause;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

public class FilterChainWhereClauseTest {
  //
  // Test Methods
  //

  @SuppressWarnings("unchecked")
  @Test
  public void testWhereWithIncludingFilterChain() {
    FilterChain<ValueSet> filterChainMock = (FilterChain<ValueSet>) createMock(FilterChain.class);
    ValueSet valueSetMock = createMock(ValueSet.class);

    expect(filterChainMock.filter(valueSetMock)).andReturn(valueSetMock);
    replay(filterChainMock);

    WhereClause filterChainWhereClause = new FilterChainWhereClause(filterChainMock);
    boolean result = filterChainWhereClause.where(valueSetMock);

    // Verify behaviour.
    verify(filterChainMock);

    // Verify state.
    assertThat(result).isTrue();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testWhereWithExcludingFilterChain() {
    FilterChain<ValueSet> filterChainMock = (FilterChain<ValueSet>) createMock(FilterChain.class);
    ValueSet valueSetMock = createMock(ValueSet.class);

    expect(filterChainMock.filter(valueSetMock)).andReturn(null);
    replay(filterChainMock);

    WhereClause filterChainWhereClause = new FilterChainWhereClause(filterChainMock);
    boolean result = filterChainWhereClause.where(valueSetMock);

    // Verify behaviour.
    verify(filterChainMock);

    // Verify state.
    assertThat(result).isFalse();
  }
}
