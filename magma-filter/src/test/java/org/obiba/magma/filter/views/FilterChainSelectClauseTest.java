package org.obiba.magma.filter.views;

import org.junit.Test;
import org.obiba.magma.Variable;
import org.obiba.magma.filter.FilterChain;
import org.obiba.magma.views.SelectClause;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

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

    SelectClause filterChainSelectClause = new FilterChainSelectClause(filterChainMock);
    boolean result = filterChainSelectClause.select(variableMock);

    // Verify behaviour.
    verify(filterChainMock);

    // Verify state.
    assertThat(result).isTrue();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSelectWithExcludingFilterChain() {
    FilterChain<Variable> filterChainMock = (FilterChain<Variable>) createMock(FilterChain.class);
    Variable variableMock = createMock(Variable.class);

    expect(filterChainMock.filter(variableMock)).andReturn(null);
    replay(filterChainMock);

    SelectClause filterChainSelectClause = new FilterChainSelectClause(filterChainMock);
    boolean result = filterChainSelectClause.select(variableMock);

    // Verify behaviour.
    verify(filterChainMock);

    // Verify state.
    assertThat(result).isFalse();

  }
}
