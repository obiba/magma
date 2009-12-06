package org.obiba.magma;

import java.util.Set;

public interface Datasource extends Initialisable, Disposable {

  public String getName();

  public String getType();

  public DatasourceMetaData getMetaData();

  public ValueTable getValueTable(String name) throws NoSuchValueTableException;

  public Set<ValueTable> getValueTables();

  public ValueTableWriter createWriter(String tableName);

}
