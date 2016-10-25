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

import com.google.common.collect.Lists;
import org.obiba.magma.*;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.magma.support.ValueSetBean;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
class JoinValueSet extends ValueSetBean {

  private final JoinValueSetFetcher fetcher;

  private List<ValueSet> innerValueSets;

  JoinValueSet(@NotNull JoinTable table, @NotNull VariableEntity entity) {
    super(table, entity);
    this.fetcher = new JoinValueSetFetcher(table);
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    List<Timestamps> timestampses = Lists.newArrayList();
    timestampses.addAll(((JoinTable) getValueTable()).getTables().stream() //
        .filter(valueTable -> valueTable.hasValueSet(getVariableEntity())) //
        .map(valueTable -> valueTable.getValueSetTimestamps(getVariableEntity())) //
        .collect(Collectors.toList()));
    return new UnionTimestamps(timestampses);
  }

  public Value getValue(Variable variable) {
    // for each inner value sets and get the value (if the inner table has value set and variable defined)
    // and wrap result in a value sequence
    List<Value> values = getInnerTableValueSets().stream() //
        .map(joinedValueSet -> joinedValueSet == null || !hasWrappedVariable(joinedValueSet.getValueTable(), variable) ?
            variable.getValueType().nullValue() :
            getWrappedVariable(joinedValueSet.getValueTable(), variable).getValue(joinedValueSet))
        .collect(Collectors.toList());
    return variable.getValueType().sequenceOf(values);
  }

  private List<ValueSet> getInnerTableValueSets() {
    if (innerValueSets == null) {
      innerValueSets = fetcher.getInnerTableValueSets(getVariableEntity());
    }
    return innerValueSets;
  }

  public void setInnerValueSets(List<ValueSet> innerValueSets) {
    this.innerValueSets = innerValueSets;
  }

  private boolean hasWrappedVariable(ValueTable table, Variable variable) {
    return table.hasVariable(variable.getName());
  }

  private VariableValueSource getWrappedVariable(ValueTable table, Variable variable) {
    return table.getVariableValueSource(variable.getName());
  }

}
