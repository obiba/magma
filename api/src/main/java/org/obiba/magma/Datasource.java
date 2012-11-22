package org.obiba.magma;

import java.util.Set;

public interface Datasource extends Initialisable, Disposable, AttributeAware {

  String getName();

  String getType();

  boolean hasValueTable(String tableName);

  ValueTable getValueTable(String tableName) throws NoSuchValueTableException;

  Set<ValueTable> getValueTables();

  boolean canDropTable(String tableName);

  void dropTable(String tableName);

  ValueTableWriter createWriter(String tableName, String entityType);

  void setAttributeValue(String name, Value value);

}
