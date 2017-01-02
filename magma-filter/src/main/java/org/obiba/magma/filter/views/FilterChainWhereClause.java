/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.filter.views;

import org.obiba.magma.ValueSet;
import org.obiba.magma.filter.FilterChain;
import org.obiba.magma.views.View;
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
    return where(valueSet, null);
  }

  @Override
  public boolean where(ValueSet valueSet, View view) {
    if(filterChain == null) {
      throw new IllegalStateException("Null filterChain");
    }
    return filterChain.filter(valueSet) != null;
  }

  //
  // Methods
  //

  public void setFilterChain(FilterChain<ValueSet> filterChain) {
    this.filterChain = filterChain;
  }
}
