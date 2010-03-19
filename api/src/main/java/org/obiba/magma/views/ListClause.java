package org.obiba.magma.views;

import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.VariableValueSource;

/**
 * This clause allows a list of derived variables to be specified. The {@code ListClause} is mutually exclusive to the
 * {@link SelectClause}.
 */
public interface ListClause {

  /**
   * Lists the {@link VariableValueSource}s that are part of the {@link View}. The {@code VariableValueSource} provides
   * access to both the {@link Variable} metadata as well as the actual {@link Value}.
   * 
   * @return a list of {@code VariableValueSource}s that are part of the {@code View}.
   */
  public Iterable<VariableValueSource> getVariableValueSources();

  /**
   * Gets a {@link VariableValueSource} by name from the current {@code View}.
   * @param name The {@link Variable} name associated with the {@code VariableValueSource} requested
   * @return a {@code VariableValueSource} matching the provided name.
   * @throws NoSuchVariableException If a {@code Variable} (and thus a {@code VariableValueSource) does not exist with
   * the provided name.
   */
  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException;

}
