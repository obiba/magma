/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 */
package org.obiba.magma.views;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.transform.TransformingValueTable;

public class ValueSetWrapper implements ValueSet {

//  private static final Logger log = LoggerFactory.getLogger(ValueSetWrapper.class);

  @NotNull
  private final TransformingValueTable mappingTable;

  @NotNull
  private final ValueSet wrapped;

  ValueSetWrapper(@NotNull TransformingValueTable mappingTable, @NotNull ValueSet wrapped) {
    this.mappingTable = mappingTable;
    this.wrapped = wrapped;
  }

  @Override
  public ValueTable getValueTable() {
    return mappingTable;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return mappingTable.getVariableEntityMappingFunction().apply(wrapped.getVariableEntity());
  }

  public ValueSet getWrappedValueSet() {
    return wrapped;
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return wrapped.getTimestamps();
  }
}