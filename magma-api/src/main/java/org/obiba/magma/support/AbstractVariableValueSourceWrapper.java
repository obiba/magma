/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.support;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceWrapper;
import org.obiba.magma.VectorSource;

/**
 *
 */
public abstract class AbstractVariableValueSourceWrapper implements VariableValueSourceWrapper {

  @NotNull
  private final VariableValueSource wrapped;

  @SuppressWarnings("ConstantConditions")
  public AbstractVariableValueSourceWrapper(@NotNull VariableValueSource wrapped) {
    if(wrapped == null) throw new IllegalArgumentException("wrapped VariableValueSource cannot be null");
    this.wrapped = wrapped;
  }

  @NotNull
  @Override
  public VariableValueSource getWrapped() {
    return wrapped;
  }

  @Override
  public Variable getVariable() {
    return wrapped.getVariable();
  }

  @NotNull
  @Override
  public ValueType getValueType() {
    return wrapped.getValueType();
  }

  @Override
  @NotNull
  public Value getValue(ValueSet valueSet) {
    return wrapped.getValue(valueSet);
  }

  @Nullable
  @Override
  public VectorSource asVectorSource() {
    return wrapped.asVectorSource();
  }

  @Override
  public String getName() {
    return wrapped.getName();
  }

}
