package org.obiba.magma.spring;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

public interface ValueTableFactoryBean {

  public ValueTable buildValueTable(Datasource datasource);

}
