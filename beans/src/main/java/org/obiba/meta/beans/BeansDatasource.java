package org.obiba.meta.beans;

import org.obiba.meta.Datasource;
import org.obiba.meta.ValueSet;

public interface BeansDatasource extends Datasource, ValueSetBeanResolver {

  public BeanValueSetConnection createConnection(ValueSet valueSet);

}
