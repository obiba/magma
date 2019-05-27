/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.filter;

import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractValueTableWrapper;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FilteredValueTable extends AbstractValueTableWrapper {

  private final FilterChain<ValueSet> entityFilterChain;

  private final FilterChain<Variable> variableFilterChain;

  private final ValueTable valueTable;

  public FilteredValueTable(ValueTable valueTable, FilterChain<Variable> variableFilterChain,
      FilterChain<ValueSet> entityFilterChain) {
    this.valueTable = valueTable;
    this.entityFilterChain = entityFilterChain;
    this.variableFilterChain = variableFilterChain;
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return valueTable;
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    return entityFilterChain.filter(getWrappedValueTable().getValueSet(entity)) != null;
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    ValueSet valueSet = super.getValueSet(entity);
    if(entityFilterChain.filter(valueSet) == null) {
      throw new NoSuchValueSetException(this, entity);
    }
    return valueSet;
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return StreamSupport.stream(getWrappedValueTable().getValueSets().spliterator(), false)
        .filter(input -> entityFilterChain.filter(input) != null).collect(Collectors.toList());
  }

  @Override
  public Iterable<Variable> getVariables() {
    return StreamSupport.stream(getWrappedValueTable().getVariables().spliterator(), false)
        .filter(input -> variableFilterChain.filter(input) != null).collect(Collectors.toList());
  }

}
