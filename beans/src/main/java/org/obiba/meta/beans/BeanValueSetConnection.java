package org.obiba.meta.beans;

import org.obiba.meta.ValueSetConnection;
import org.obiba.meta.Variable;

public interface BeanValueSetConnection extends ValueSetConnection {

  public <B> B findBean(Class<B> type, Variable variable);

}
