/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.filter;

import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableValueSource;

/**
 * The StateEnvelope is a container holding the item to be filtered as well as its current state: indicating if its IN
 * or OUT of the result set. The StateEnvelope is passed to a {@link Filter} which filters the item inside and then
 * adjusts the state in the envelope if necessary.
 *
 * @param <T> the item being filtered, {@link ValueSet} or {@link VariableValueSource}.
 */
public class StateEnvelope<T> {

  private FilterState state;

  private final T item;

  public StateEnvelope(T item) {
    this.item = item;
    state = FilterState.IN;
  }

  public FilterState getState() {
    return state;
  }

  public T getItem() {
    return item;
  }

  public boolean isState(FilterState state) {
    return this.state == state;
  }

  void setState(FilterState state) {
    this.state = state;
  }

}
