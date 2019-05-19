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

import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.NullTimestamps;

/**
 * A {@link ValueSet} that represents a missing value set (when joining a table that has no associated entity).
 */
class EmptyValueSet implements ValueSet {

  private final ValueTable table;

  private final VariableEntity entity;

  EmptyValueSet(ValueTable table, VariableEntity entity) {
    this.table = table;
    this.entity = entity;
  }

  @Override
  public ValueTable getValueTable() {
    return table;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return entity;
  }

  @Override
  public Timestamps getTimestamps() {
    return NullTimestamps.get();
  }
}
