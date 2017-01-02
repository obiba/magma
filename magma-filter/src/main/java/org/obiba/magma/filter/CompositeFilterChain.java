/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.filter;

import java.util.ArrayList;
import java.util.List;

public class CompositeFilterChain<T> implements FilterChain<T> {
  //
  // Instance Variables
  //

  private final String entityType;

  private final List<FilterChain<T>> filterChains;

  //
  // Constructors
  //

  public CompositeFilterChain(String entityType) {
    if(entityType == null) {
      throw new IllegalArgumentException("null entityType");
    }
    this.entityType = entityType;

    filterChains = new ArrayList<>();
  }

  //
  // FilterChain Methods
  //

  @Override
  public T filter(T item) {
    if(filterChains.isEmpty()) {
      return item;
    }

    T result = item;
    for(FilterChain<T> filterChain : filterChains) {
      result = filterChain.filter(result);
      if(result == null) break;
    }
    return result;
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  //
  // Methods
  //

  public void addFilterChain(FilterChain<T> filterChain) {
    if(!filterChain.getEntityType().equals(getEntityType())) {
      throw new IllegalArgumentException("filter chain does not have the expected entity type");
    }
    filterChains.add(filterChain);
  }
}
