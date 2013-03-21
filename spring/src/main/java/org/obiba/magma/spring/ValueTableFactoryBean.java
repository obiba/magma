package org.obiba.magma.spring;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

public interface ValueTableFactoryBean {

  String getValueTableName();

  ValueTable buildValueTable(Datasource datasource);

}
