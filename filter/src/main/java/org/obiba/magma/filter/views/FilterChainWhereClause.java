package org.obiba.magma.filter.views;

import org.obiba.magma.ValueSet;
import org.obiba.magma.filter.FilterChain;
import org.obiba.magma.views.WhereClause;

public class FilterChainWhereClause implements WhereClause {
  //
  // Instance Variables
  //

  private FilterChain<ValueSet> filterChain;

  //
  // Constructors
  //

  public FilterChainWhereClause(FilterChain<ValueSet> filterChain) {
    this.filterChain = filterChain;
  }

  //
  // WhereClause Methods
  //

  @Override
  public boolean where(ValueSet valueSet) {
    return (filterChain.filter(valueSet) != null);
  }

}
