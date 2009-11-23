package org.obiba.magma.filter;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class AbstractFilter<T> implements Filter<T> {

  protected static final String EXCLUDE = "exclude";

  protected static final String INCLUDE = "include";

  @XStreamAsAttribute
  protected String type;

  protected abstract boolean runFilter(T item);

  @Override
  public StateEnvelope<T> doIt(StateEnvelope<T> stateEnvelope) {
    if(type == null) type = EXCLUDE;
    if(type.equalsIgnoreCase(EXCLUDE) && stateEnvelope.getState().equals(FilterState.OUT)) {
      return stateEnvelope;
    } else if(type.equalsIgnoreCase(INCLUDE) && stateEnvelope.getState().equals(FilterState.IN)) {
      return stateEnvelope;
    } else {
      return updateStateEnvelope(stateEnvelope);
    }

  }

  private StateEnvelope<T> updateStateEnvelope(StateEnvelope<T> stateEnvelope) {
    if(runFilter(stateEnvelope.getItem())) {
      if(type.equalsIgnoreCase(EXCLUDE)) {
        stateEnvelope.setState(FilterState.OUT);
      } else if(type.equalsIgnoreCase(INCLUDE)) {
        stateEnvelope.setState(FilterState.IN);
      }
    }
    return stateEnvelope;
  }

  /**
   * Ensures the filter type (include or exclude) has been set correctly.
   * @throws IllegalArgumentException When the type has not been set correctly.
   */
  protected void validateType() {
    String errorMessage = "The argument [type] must have the value [include] or [exclude].";
    if(type == null) throw new IllegalArgumentException(errorMessage);
    if(!type.equalsIgnoreCase(EXCLUDE) && !type.equalsIgnoreCase(INCLUDE)) throw new IllegalArgumentException(errorMessage);
  }


  protected void setType(String type) {
    this.type = type;
  }

  public static class Builder {

    protected String type;

    public Builder include() {
      this.type = INCLUDE;
      return this;
    }

    public Builder exclude() {
      this.type = EXCLUDE;
      return this;
    }
  }
}
