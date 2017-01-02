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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class AbstractFilter<T> implements Filter<T> {

  private static final Logger log = LoggerFactory.getLogger(AbstractFilter.class);

  protected enum Type {
    /**
     * Exclude item from the result set if filter successful.
     */
    EXCLUDE,
    /**
     * Include item in the result set if filter successful.
     */
    INCLUDE
  }

  @XStreamAsAttribute
  private Type type;

  protected abstract Boolean runFilter(T item);

  @Override
  public StateEnvelope<T> doIt(StateEnvelope<T> stateEnvelope) {
    if(type == null) type = Type.EXCLUDE;
    if(isExclude() && stateEnvelope.isState(FilterState.OUT)) {
      return stateEnvelope;
    }
    if(isInclude() && stateEnvelope.isState(FilterState.IN)) {
      return stateEnvelope;
    }
    return updateStateEnvelope(stateEnvelope);
  }

  private StateEnvelope<T> updateStateEnvelope(StateEnvelope<T> stateEnvelope) {
    Boolean result = runFilter(stateEnvelope.getItem());

    if(result == null) {
      log.error("The filter [{}] returned a null value. This filter is being ignored.", this);
      return stateEnvelope;
    }

    if(result) {
      if(isExclude()) {
        stateEnvelope.setState(FilterState.OUT);
      } else if(isInclude()) {
        stateEnvelope.setState(FilterState.IN);
      }
    }
    return stateEnvelope;
  }

  protected void setType(Type type) {
    this.type = type;
  }

  protected boolean isInclude() {
    return type == Type.INCLUDE;
  }

  protected boolean isExclude() {
    return type == Type.EXCLUDE;
  }

  public static class Builder {

    protected Type type;

    public Builder include() {
      type = Type.INCLUDE;
      return this;
    }

    public Builder exclude() {
      type = Type.EXCLUDE;
      return this;
    }
  }
}
