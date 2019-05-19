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

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSetWrapper;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.springframework.cache.Cache;

public class CachedValueSet implements ValueSet, ValueSetWrapper {
  private ValueSet wrapped;

  private Cache cache;

  private CachedValueTable table;

  private VariableEntity variableEntity;

  public CachedValueSet(@NotNull CachedValueTable table, @NotNull VariableEntity variableEntity, @NotNull Cache cache) {
    this.table = table;
    this.variableEntity = variableEntity;
    this.cache = cache;

    try {
      wrapped = table.getWrappedValueTable().getValueSet(variableEntity);
    } catch(MagmaRuntimeException ex) {
      //ignore
    }
  }

  @Override
  public ValueTable getValueTable() {
    return table;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return variableEntity;
  }

  @Override
  public Timestamps getTimestamps() {
    return new CachedTimestamps(this, cache);
  }

  @Override
  public ValueSet getWrapped() {
    if(wrapped == null) throw new MagmaRuntimeException("wrapped value not initialized.");
    return wrapped;
  }
}
