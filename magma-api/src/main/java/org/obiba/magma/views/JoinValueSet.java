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
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.magma.support.ValueSetBean;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
class JoinValueSet extends ValueSetBean {

  private final JoinValueSetFetcher fetcher;

  private Iterable<ValueSet> innerValueSets;

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
    // get inner value sets
    for (ValueSet joinedValueSet : getInnerTableValueSets()) {
      Value value = getWrappedVariable(joinedValueSet.getValueTable(), variable).getValue(joinedValueSet);
      if (!value.isNull()) return value;
    }
    return variable.isRepeatable() ? variable.getValueType().nullSequence() : variable.getValueType().nullValue();
  }

  private Iterable<ValueSet> getInnerTableValueSets() {
    if (innerValueSets == null) {
      innerValueSets = fetcher.getInnerTableValueSets(getVariableEntity());
    }
    return innerValueSets;
  }

  public void setInnerValueSets(Iterable<ValueSet> innerValueSets) {
    this.innerValueSets = innerValueSets;
  }

  private VariableValueSource getWrappedVariable(ValueTable table, Variable variable) {
    return table.getVariableValueSource(variable.getName());
  }

}
