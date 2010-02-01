package org.obiba.magma.filter.views;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.filter.FilterChain;

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

    FilterChainWhereClause filterChainWhereClause = new FilterChainWhereClause(filterChainMock);
    boolean result = filterChainWhereClause.where(valueSetMock);

    // Verify behaviour.
    verify(filterChainMock);

    // Verify state.
    assertEquals(true, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testWhereWithExcludingFilterChain() {
    FilterChain<ValueSet> filterChainMock = (FilterChain<ValueSet>) createMock(FilterChain.class);
    ValueSet valueSetMock = createMock(ValueSet.class);

    expect(filterChainMock.filter(valueSetMock)).andReturn(null);
    replay(filterChainMock);

    FilterChainWhereClause filterChainWhereClause = new FilterChainWhereClause(filterChainMock);
    boolean result = filterChainWhereClause.where(valueSetMock);

    // Verify behaviour.
    verify(filterChainMock);

    // Verify state.
    assertEquals(false, result);
  }
}
