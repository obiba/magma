package org.obiba.magma;

import java.util.Set;

public interface Datasource extends Initialisable, Disposable, AttributeAware {

  public String getName();

  public String getType();

  public boolean hasValueTable(String name);

  public ValueTable getValueTable(String name) throws NoSuchValueTableException;

  public Set<ValueTable> getValueTables();

  public boolean canDropTable(String name);

  public void dropTable(String name);

  public ValueTableWriter createWriter(String tableName, String entityType);

  public void setAttributeValue(String name, Value value);

}
