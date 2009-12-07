package org.obiba.magma;

import java.util.Set;

public interface Datasource extends Initialisable, Disposable, AttributeAware {

  public String getName();

  public String getType();

  public boolean hasValueTable(String name);

  public ValueTable getValueTable(String name) throws NoSuchValueTableException;

  public Set<ValueTable> getValueTables();

  public ValueTableWriter createWriter(String tableName);

}
