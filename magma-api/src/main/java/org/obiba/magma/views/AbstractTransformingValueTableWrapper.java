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

import java.util.List;

import javax.validation.constraints.NotNull;

import com.google.common.collect.*;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.AbstractValueTableWrapper;
import org.obiba.magma.support.AbstractVariableValueSourceWrapper;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.transform.BijectiveFunctions;
import org.obiba.magma.transform.TransformingValueTable;

public abstract class AbstractTransformingValueTableWrapper extends AbstractValueTableWrapper
    implements TransformingValueTable {

  private final BiMap<String, String> variableNameMapping = HashBiMap.create();

  protected void addVariableNameMapping(String variableName, String wrappedVariableName) {
    variableNameMapping.put(variableName, wrappedVariableName);
  }

  public BijectiveFunction<String, String> getVariableNameMappingFunction() {
    return new BijectiveFunction<String, String>() {

      @Override
      public String apply(String input) {
        if(variableNameMapping.inverse().containsKey(input)) {
          return variableNameMapping.inverse().get(input);
        }
        return input;
      }

      @Override
      public String unapply(String from) {
        if(variableNameMapping.containsKey(from)) {
          return variableNameMapping.get(from);
        }
        return from;
      }

    };
  }

  @Override
  public List<VariableEntity> getVariableEntities() {
    return getVariableEntities(0, -1);
  }

  @Override
  public List<VariableEntity> getVariableEntities(int offset, int limit) {
    List<VariableEntity> mappedEntities = Lists.newArrayList();
    for (VariableEntity entity : super.getVariableEntities(offset, -1)) {
      VariableEntity mappedEntity = getVariableEntityMappingFunction().apply(entity);
      // Only VariableEntities for which hasValueSet() is true (this will usually test the where clause)
      if (mappedEntity != null && hasValueSet(mappedEntity)) {
        mappedEntities.add(mappedEntity);
      }
      if (mappedEntities.size()>offset) {
        if (limit>0 && mappedEntities.size()>offset+limit) {
          break;
        }
      }
    }
    return mappedEntities.subList(offset, limit>0 && limit<mappedEntities.size() ? limit : mappedEntities.size());
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    if(entity == null) return false;
    VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
    return unmapped != null && super.hasValueSet(unmapped);
  }

  @Override
  public Iterable<ValueSet> getValueSets(Iterable<VariableEntity> entities) {
    List<VariableEntity> unmappedEntities = Lists.newArrayList();
    for (VariableEntity entity : entities) {
      VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
      if(unmapped == null) throw new NoSuchValueSetException(this, entity);
      unmappedEntities.add(unmapped);
    }
    List<ValueSet> valueSets = Lists.newArrayList();
    for (ValueSet valueSet : super.getValueSets(unmappedEntities)) {
      valueSets.add(getValueSetMappingFunction().apply(valueSet));
    }
    return valueSets;
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    Variable wrappedVariable = getWrappedValueTable()
        .getVariable(getVariableNameMappingFunction().unapply(variable.getName()));
    return super.getValue(wrappedVariable, getValueSetMappingFunction().unapply(valueSet));
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
    if(unmapped == null) throw new NoSuchValueSetException(this, entity);
    return getValueSetMappingFunction().apply(super.getValueSet(unmapped));
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    return getVariableValueSourceMappingFunction()
        .apply(super.getVariableValueSource(getVariableNameMappingFunction().unapply(variableName)));
  }

  @NotNull
  @Override
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return BijectiveFunctions.identity();
  }

  @NotNull
  @Override
  public BijectiveFunction<ValueSet, ValueSet> getValueSetMappingFunction() {
    return new BijectiveFunction<ValueSet, ValueSet>() {

      @Override
      public ValueSet unapply(ValueSet from) {
        return ((ValueSetWrapper) from).getWrappedValueSet();
      }

      @Override
      public ValueSet apply(ValueSet from) {
        return new ValueSetWrapper(AbstractTransformingValueTableWrapper.this, from);
      }
    };
  }

  @NotNull
  @Override
  public BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction() {
    return new BijectiveFunction<VariableValueSource, VariableValueSource>() {
      @Override
      public VariableValueSource apply(VariableValueSource from) {
        return new VariableValueSourceWrapper(from);
      }

      @Override
      public VariableValueSource unapply(VariableValueSource from) {
        return ((AbstractVariableValueSourceWrapper) from).getWrapped();
      }
    };
  }

  protected class VariableValueSourceWrapper extends AbstractVariableValueSourceWrapper {

    public VariableValueSourceWrapper(VariableValueSource wrapped) {
      super(wrapped);
    }

    @NotNull
    @Override
    public Variable getVariable() {
      return AbstractTransformingValueTableWrapper.this
          .getVariable(getVariableNameMappingFunction().apply(getWrapped().getVariable().getName()));
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      return getWrapped().getValue(getValueSetMappingFunction().unapply(valueSet));
    }

  }

}
