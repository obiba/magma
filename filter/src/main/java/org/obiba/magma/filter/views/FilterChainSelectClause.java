package org.obiba.magma.filter.views;

import org.obiba.magma.Variable;
import org.obiba.magma.filter.FilterChain;
import org.obiba.magma.views.SelectClause;

public class FilterChainSelectClause implements SelectClause {
  //
  // Instance Variables
  //

  private FilterChain<Variable> filterChain;

  //
  // Constructors
  //

  public FilterChainSelectClause(FilterChain<Variable> filterChain) {
    this.filterChain = filterChain;
  }

  //
  // SelectClause Methods
  //

  @Override
  public boolean select(Variable variable) {
    return (filterChain.filter(variable) != null);
  }

}
