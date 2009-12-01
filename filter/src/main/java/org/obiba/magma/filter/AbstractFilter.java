package org.obiba.magma.filter;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class AbstractFilter<T> implements Filter<T> {

  protected static enum Type {
    /** Exclude item from the result set if filter successful. */
    EXCLUDE,
    /** Include item in the result set if filter successful. */
    INCLUDE
  }

  @XStreamAsAttribute
  private Type type;

  protected abstract boolean runFilter(T item);

  @Override
  public StateEnvelope<T> doIt(StateEnvelope<T> stateEnvelope) {
    if(type == null) type = Type.EXCLUDE;
    if(isExclude() && stateEnvelope.isState(FilterState.OUT)) {
      return stateEnvelope;
    } else if(isInclude() && stateEnvelope.isState(FilterState.IN)) {
      return stateEnvelope;
    } else {
      return updateStateEnvelope(stateEnvelope);
    }

  }

  private StateEnvelope<T> updateStateEnvelope(StateEnvelope<T> stateEnvelope) {
    if(runFilter(stateEnvelope.getItem())) {
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
      this.type = Type.INCLUDE;
      return this;
    }

    public Builder exclude() {
      this.type = Type.EXCLUDE;
      return this;
    }
  }
}
