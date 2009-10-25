package org.obiba.meta.type;

import org.obiba.meta.ValueType;

public abstract class AbstractNumberType implements ValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  protected AbstractNumberType() {

  }

  public boolean isDateTime() {
    return false;
  }

  public boolean isNumeric() {
    return true;
  }

}
