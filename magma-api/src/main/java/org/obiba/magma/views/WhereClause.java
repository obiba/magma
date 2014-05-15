package org.obiba.magma.views;

import org.obiba.magma.ValueSet;

/**
 * Interface for abstracting how {@link ValueSet} instances are selected.
 */
public interface WhereClause {

  /**
   * Indicates whether the specified value set is selected by this clause.
   *
   * @param valueSet a value set
   * @return <code>true</code> if selected
   */
  boolean where(ValueSet valueSet);

  /**
   * Indicates whether the specified value set is selected by this clause in the context of a view.
   * @param valueSet
   * @param view
   * @return
   */
  boolean where(ValueSet valueSet, View view);
}
