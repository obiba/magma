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

import java.util.List;
import java.util.stream.Collectors;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

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

  /**
   * Selects the entities of a table that are selected by this clause in the context of a view. Value sets are got one
   * by one, which costs no read on most tables until a value is asked for. A clause that reads values can do better,
   * for instance by reading them in bulk.
   *
   * @param table the table the entities belong to
   * @param entities the entities to filter
   * @param view the view
   * @return the selected entities, in the order of the given ones
   */
  default List<VariableEntity> filter(ValueTable table, List<VariableEntity> entities, View view) {
    return entities.stream()
        .filter(entity -> where(table.getValueSet(entity), view))
        .collect(Collectors.toList());
  }
}
