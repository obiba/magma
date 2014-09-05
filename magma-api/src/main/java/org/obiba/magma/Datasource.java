package org.obiba.magma;

import java.util.Set;

import javax.validation.constraints.NotNull;

import com.google.common.base.Predicate;

/**
 * A datasource is a container of {@link org.obiba.magma.ValueTable}s and is responsible for maintaining the access to
 * each of them, especially when build over a persistence layer (file, database).
 */
public interface Datasource extends Initialisable, Disposable, AttributeAware, Timestamped {

  /**
   * Required name of the datasource.
   * @return
   */
  String getName();

  /**
   * Keyword name that identifies the specific implementation of this datasource.
   * @return
   */
  String getType();

  /**
   * Verifies that a {@link org.obiba.magma.ValueTable} exists with this name.
   * @param tableName
   * @return
   */
  boolean hasValueTable(String tableName);

  /**
   * Verifies if any of the {@link org.obiba.magma.ValueTable}s satisfy the given predicate.
   * @param predicate
   * @return
   */
  // TODO rename to hasValueTables
  boolean hasEntities(Predicate<ValueTable> predicate);

  /**
   * Get the {@link org.obiba.magma.ValueTable} with the given name.
   * @param tableName
   * @return
   * @throws NoSuchValueTableException
   * @see #hasValueTable(String)
   */
  ValueTable getValueTable(String tableName) throws NoSuchValueTableException;

  /**
   * Get all {@link org.obiba.magma.ValueTable}s.
   * @return
   */
  Set<ValueTable> getValueTables();

  /**
   * Verify if the {@link org.obiba.magma.ValueTable} with the given name can be deleted.
   * @param tableName
   * @return
   */
  boolean canDropTable(String tableName);

  /**
   * Delete the {@link org.obiba.magma.ValueTable} with the given name.
   * @param tableName
   * @see #canDropTable(String)
   */
  void dropTable(String tableName);

  /**
   * Verify if the {@link org.obiba.magma.ValueTable} with the given name can be renamed.
   * @param tableName
   * @return
   */
  boolean canRenameTable(String tableName);

  /**
   * Rename the {@link org.obiba.magma.ValueTable} with the given name.
   * @param tableName
   * @param newName
   * @see #canRenameTable(String)
   */
  void renameTable(String tableName, String newName);

  /**
   * Delete the datasource and associated {@link org.obiba.magma.ValueTable}s.
   * @see #canDrop()
   */
  void drop();

  /**
   * Verifies that this datasource supports deletion.
   * @return
   */
  boolean canDrop();

  /**
   * Build a {@link org.obiba.magma.ValueTableWriter} for this datasource.
   * @param tableName
   * @param entityType
   * @return
   */
  @NotNull
  ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType);

  /**
   * Set the {@link org.obiba.magma.Attribute} value.
   * @param name
   * @param value
   */
  void setAttributeValue(String name, Value value);

}
