package org.obiba.magma.type;

import org.obiba.magma.Value;

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

  @Override
  public boolean isGeo() {
    return false;
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public int compare(Value o1, Value o2) {
    Comparable l1 = (Comparable) o1.getValue();
    Comparable l2 = (Comparable) o2.getValue();
    if(l1 == l2) return 0;
    if(l1 == null) return -1;
    if(l2 == null) return 1;
    return l1.compareTo(l2);
  }
}
