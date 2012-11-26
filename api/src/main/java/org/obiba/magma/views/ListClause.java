package org.obiba.magma.views;

import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.VariableValueSource;

/**
 * This clause allows a list of derived variables to be specified. The {@code ListClause} is mutually exclusive to the
 * {@link SelectClause}.
 */
public interface ListClause {

  /**
   * Lists the {@link VariableValueSource}s that are part of the {@link View}. The {@code VariableValueSource} provides
   * access to both the {@link Variable} metadata as well as the actual {@link Value}.
   * @return a list of {@code VariableValueSource}s that are part of the {@code View}.
   */
  Iterable<VariableValueSource> getVariableValueSources();

  /**
   * Gets a {@link VariableValueSource} by name from the current {@code View}.
   * @param name The {@link Variable} name associated with the {@code VariableValueSource} requested
   * @return a {@code VariableValueSource} matching the provided name.
   * @throws NoSuchVariableException If a {@code Variable} (and thus a {@code VariableValueSource) does not exist with
   * the provided name.
   */
  VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException;

  /**
   * Provide the {@link ValueTable} to the ListClause. The {@code ListClause} will need the {@code ValueTable} in order
   * to retrieve {@link Variable}s referenced with the "sameAs" attribute.
   * @param valueTable The table to be set in the {@code ListClause}
   */
  void setValueTable(ValueTable valueTable);

  /**
   * Gets a {@link VariableWriter} to update derived variables list.
   * @return
   */
  VariableWriter createWriter();

}
