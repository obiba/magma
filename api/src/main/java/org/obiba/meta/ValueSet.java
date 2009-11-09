package org.obiba.meta;

import java.util.Date;

public interface ValueSet {

  public ValueSetProvider getValueSetProvider();

  public VariableEntity getVariableEntity();

  public Date getStartDate();

  public Date getEndDate();

}
