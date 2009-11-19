package org.obiba.magma.filter;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class AbstractFilter<T> implements Filter<T> {

  protected static final String EXCLUDE = "EXCLUDE";

  protected static final String INCLUDE = "INCLUDE";

  @XStreamAsAttribute
  protected String type;

  abstract boolean runFilter(T item);

  /*
   * (non-Javadoc)
   * 
   * @see org.obiba.magma.filter.Filter#doIt(org.obiba.magma.filter.StateEnvelope)
   */
  @Override
  public StateEnvelope<T> doIt(StateEnvelope<T> stateEnvelope) {
    if(type == null) type = "exclude";
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

  private Object readResolve() {
    if(type == null) System.out.println("do ya doo.... type is null");
    return this;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }
}
