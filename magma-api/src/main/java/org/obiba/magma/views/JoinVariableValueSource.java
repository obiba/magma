/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views;

import com.google.common.collect.Iterables;
import org.obiba.magma.*;
import org.obiba.magma.support.AbstractVariableValueSourceWrapper;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by yannick on 04/10/16.
 */
public class JoinVariableValueSource extends AbstractVariableValueSourceWrapper implements VectorSource {

  @NotNull
  private final List<ValueTable> owners;

  @NotNull
  private final String variableName;

  JoinVariableValueSource(@NotNull String variableName, @NotNull List<ValueTable> owners,
                          @NotNull VariableValueSource wrapped) {
    super(wrapped);
    this.variableName = variableName;
    this.owners = owners;
  }

  private VariableValueSource getWrapped(ValueTable table) {
    return table.getVariableValueSource(variableName);
  }

  @NotNull
  @Override
  public Value getValue(ValueSet valueSet) {
    return ((JoinValueSet) valueSet).getValue(getVariable());
  }

  @Override
  public boolean supportVectorSource() {
    for (ValueTable table : owners) {
      if (!table.getVariableValueSource(variableName).supportVectorSource()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public VectorSource asVectorSource() {
    return this;
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) return true;
    if (that instanceof JoinVariableValueSource) {
      JoinVariableValueSource jvvs = (JoinVariableValueSource) that;
      return getWrapped().equals(jvvs.getWrapped()) && Iterables.elementsEqual(owners, jvvs.owners);
    }
    return super.equals(that);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 37 * result + owners.hashCode();
    result = 37 * result + getWrapped().hashCode();
    return result;
  }

  @Override
  public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
    return () -> new JoinValueIterator(entities, owners, getVariable());
  }

}
