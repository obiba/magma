package org.obiba.magma.type;

public abstract class AbstractNumberType extends AbstractValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  protected AbstractNumberType() {

  }

  @Override
  public boolean isDateTime() {
    return false;
  }

  @Override
  public boolean isNumeric() {
    return true;
  }
}
