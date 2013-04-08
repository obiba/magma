package org.obiba.magma.views;

import org.obiba.magma.Variable;

/**
 * Interface for abstracting how {@link Variable} instances are selected.
 */
public interface SelectClause {

  /**
   * Indicates whether the specified variable is selected by this clause.
   *
   * @param variable a variable
   * @return <code>true</code> if selected
   */
  boolean select(Variable variable);
}
