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

  /**
   * No-arg constructor for XStream.
   */
  public FilterChainSelectClause() {

  }

  public FilterChainSelectClause(FilterChain<Variable> filterChain) {
    this.filterChain = filterChain;
  }

  //
  // SelectClause Methods
  //

  @Override
  public boolean select(Variable variable) {
    if(filterChain == null) {
      throw new IllegalStateException("Null filterChain");
    }
    return (filterChain.filter(variable) != null);
  }

  //
  // Methods
  //

  public void setFilterChain(FilterChain<Variable> filterChain) {
    this.filterChain = filterChain;
  }
}
