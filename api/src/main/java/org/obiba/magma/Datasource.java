package org.obiba.magma;

import java.util.Set;

public interface Datasource extends Initialisable {

  public String getName();

  public String getType();

  public ValueTable getValueTable(String name);

  public Set<ValueTable> getValueTables();

}
