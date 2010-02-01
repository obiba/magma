package org.obiba.magma.filter.views;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.obiba.magma.Variable;
import org.obiba.magma.filter.FilterChain;

public class FilterChainSelectClauseTest {
  //
  // Test Methods
  //

  @SuppressWarnings("unchecked")
  @Test
  public void testSelectWithIncludingFilterChain() {
    FilterChain<Variable> filterChainMock = (FilterChain<Variable>) createMock(FilterChain.class);
    Variable variableMock = createMock(Variable.class);

    expect(filterChainMock.filter(variableMock)).andReturn(variableMock);
    replay(filterChainMock);

    FilterChainSelectClause filterChainSelectClause = new FilterChainSelectClause(filterChainMock);
    boolean result = filterChainSelectClause.select(variableMock);

    // Verify behaviour.
    verify(filterChainMock);

    // Verify state.
    assertEquals(true, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSelectWithExcludingFilterChain() {
    FilterChain<Variable> filterChainMock = (FilterChain<Variable>) createMock(FilterChain.class);
    Variable variableMock = createMock(Variable.class);

    expect(filterChainMock.filter(variableMock)).andReturn(null);
    replay(filterChainMock);

    FilterChainSelectClause filterChainSelectClause = new FilterChainSelectClause(filterChainMock);
    boolean result = filterChainSelectClause.select(variableMock);

    // Verify behaviour.
    verify(filterChainMock);

    // Verify state.
    assertEquals(false, result);
  }
}
