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
