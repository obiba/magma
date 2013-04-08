package org.obiba.magma;

import java.util.Set;

import javax.annotation.Nonnull;

public interface Datasource extends Initialisable, Disposable, AttributeAware {

  String getName();

  String getType();

  boolean hasValueTable(String tableName);

  ValueTable getValueTable(String tableName) throws NoSuchValueTableException;

  Set<ValueTable> getValueTables();

  boolean canDropTable(String tableName);

  void dropTable(String tableName);

  @Nonnull
  ValueTableWriter createWriter(@Nonnull String tableName, @Nonnull String entityType);

  void setAttributeValue(String name, Value value);

}
