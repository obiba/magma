/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
