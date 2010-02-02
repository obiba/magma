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

  /**
   * No-arg constructor for XStream.
   */
  public FilterChainWhereClause() {

  }

  public FilterChainWhereClause(FilterChain<ValueSet> filterChain) {
    this.filterChain = filterChain;
  }

  //
  // WhereClause Methods
  //

  @Override
  public boolean where(ValueSet valueSet) {
    if(filterChain == null) {
      throw new IllegalStateException("Null filterChain");
    }
    return (filterChain.filter(valueSet) != null);
  }

  //
  // Methods
  //

  public void setFilterChain(FilterChain<ValueSet> filterChain) {
    this.filterChain = filterChain;
  }
}
