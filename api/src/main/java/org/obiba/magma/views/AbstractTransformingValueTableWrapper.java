package org.obiba.magma.views;

import java.util.Set;

import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.AbstractValueTableWrapper;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.transform.BijectiveFunctions;
import org.obiba.magma.transform.TransformingValueTable;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public abstract class AbstractTransformingValueTableWrapper extends AbstractValueTableWrapper
    implements TransformingValueTable {

  private final BiMap<String, String> variableNameMapping = HashBiMap.<String, String>create();

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
  public Set<VariableEntity> getVariableEntities() {
    return ImmutableSet.copyOf(Iterables
        .filter(Iterables.transform(super.getVariableEntities(), getVariableEntityMappingFunction()),
            new Predicate<VariableEntity>() {

              @Override
              public boolean apply(VariableEntity input) {
                // Only VariableEntities for which hasValueSet() is true (this will usually test the where clause)
                return hasValueSet(input);
              }

            }));
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    if(entity == null) return false;
    VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
    return unmapped != null && super.hasValueSet(unmapped);
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    // Transform the Iterable, replacing each ValueSet with one that points at the current View.
    return Iterables
        .filter(Iterables.transform(super.getValueSets(), getValueSetMappingFunction()), new Predicate<ValueSet>() {

          @Override
          public boolean apply(ValueSet input) {
            return input.getVariableEntity() != null;
          }
        });
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
  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    return getVariableValueSourceMappingFunction()
        .apply(super.getVariableValueSource(getVariableNameMappingFunction().unapply(name)));
  }

  @Override
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return BijectiveFunctions.identity();
  }

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

  @Override
  public BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction() {
    return new BijectiveFunction<VariableValueSource, VariableValueSource>() {
      @Override
      public VariableValueSource apply(VariableValueSource from) {
        return new VariableValueSourceWrapper(from);
      }

      @Override
      public VariableValueSource unapply(VariableValueSource from) {
        return ((VariableValueSourceWrapper) from).getWrapped();
      }
    };
  }

  protected class VariableValueSourceWrapper implements VariableValueSource {

    private final VariableValueSource wrapped;

    public VariableValueSourceWrapper(VariableValueSource wrapped) {
      this.wrapped = wrapped;
    }

    public VariableValueSource getWrapped() {
      return wrapped;
    }

    @Override
    public Variable getVariable() {
      return AbstractTransformingValueTableWrapper.this
          .getVariable(getVariableNameMappingFunction().apply(wrapped.getVariable().getName()));
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      return wrapped.getValue(getValueSetMappingFunction().unapply(valueSet));
    }

    @Override
    public ValueType getValueType() {
      return wrapped.getValueType();
    }

    @Override
    public VectorSource asVectorSource() {
      return wrapped.asVectorSource();
    }
  }

}
