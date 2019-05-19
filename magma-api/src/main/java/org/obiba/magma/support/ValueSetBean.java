/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

public class ValueSetBean implements ValueSet {

  @NotNull
  private final ValueTable table;

  @NotNull
  private final VariableEntity entity;

  @SuppressWarnings("ConstantConditions")
  public ValueSetBean(@NotNull ValueTable table, @NotNull VariableEntity entity) {
    if(table == null) throw new IllegalArgumentException("table cannot be null");
    if(entity == null) throw new IllegalArgumentException("entity cannot be null");
    this.table = table;
    this.entity = entity;
  }

  protected ValueSetBean(ValueSet valueSet) {
    this(valueSet.getValueTable(), valueSet.getVariableEntity());
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return getValueTable().getTimestamps();
  }

  @Override
  @NotNull
  public ValueTable getValueTable() {
    return table;
  }

  @Override
  @NotNull
  public VariableEntity getVariableEntity() {
    return entity;
  }

  @Override
  public String toString() {
    return "valueSet[" + getValueTable() + ":" + getVariableEntity() + "]";
  }

}
