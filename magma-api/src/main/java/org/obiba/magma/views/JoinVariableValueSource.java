/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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
 *
 */
public class JoinVariableValueSource implements VariableValueSource, VectorSource {

  @NotNull
  private final Variable joinableVariable;

  @NotNull
  private final List<ValueTable> owners;

  JoinVariableValueSource(@NotNull Variable joinableVariable, @NotNull List<ValueTable> owners) {
    this.joinableVariable = joinableVariable;
    this.owners = owners;
  }

  @Override
  public ValueType getValueType() {
    return joinableVariable.getValueType();
  }

  @NotNull
  @Override
  public Value getValue(ValueSet valueSet) {
    return ((JoinValueSet) valueSet).getValue(getVariable());
  }

  @Override
  public boolean supportVectorSource() {
    for (ValueTable table : owners) {
      if (!table.getVariableValueSource(getName()).supportVectorSource()) {
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
      return joinableVariable.equals(jvvs.joinableVariable) && Iterables.elementsEqual(owners, jvvs.owners);
    }
    return super.equals(that);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 37 * result + owners.hashCode();
    result = 37 * result + joinableVariable.hashCode();
    return result;
  }

  @Override
  public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
    return () -> new JoinValueIterator(entities, owners, getVariable());
  }

  @Override
  public String getName() {
    return joinableVariable.getName();
  }

  @Override
  public Variable getVariable() {
    return joinableVariable;
  }
}
