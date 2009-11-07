package org.obiba.meta;

import java.util.Date;

public interface ValueSet {

  public Collection getCollection();

  public VariableEntity getVariableEntity();

  public Date getStartDate();

  public Date getEndDate();

  public <T> T adapt(Class<T> type);

}
