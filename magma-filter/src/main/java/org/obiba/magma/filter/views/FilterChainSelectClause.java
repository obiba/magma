/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    return filterChain.filter(variable) != null;
  }

  //
  // Methods
  //

  public void setFilterChain(FilterChain<Variable> filterChain) {
    this.filterChain = filterChain;
  }
}
