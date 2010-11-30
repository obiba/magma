package org.obiba.magma.views;

import java.util.Set;

import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public abstract class AbstractTransformingValueTableWrapper extends AbstractValueTableWrapper implements TransformingValueTable {

  //
  // AbstractValueTableWrapper Methods
  //

  @Override
  public Set<VariableEntity> getVariableEntities() {

    return ImmutableSet.copyOf(Iterables.filter(Iterables.transform(super.getVariableEntities(), getVariableEntityMappingFunction()), new Predicate<VariableEntity>() {

      @Override
      public boolean apply(VariableEntity input) {
        // Only VariableEntities for which hasValueSet() is true (this will usually test the where clause)
        return hasValueSet(input);
      }

    }));
  }

  @Override
  public Timestamps getTimestamps(ValueSet valueSet) {
    return super.getTimestamps(((ValueSetWrapper) valueSet).getWrappedValueSet());
  }

  public boolean hasValueSet(VariableEntity entity) {
    if(entity == null) return false;
    VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
    if(unmapped == null) return false;

    return super.hasValueSet(unmapped);
  }

  public Iterable<ValueSet> getValueSets() {
    // Transform the Iterable, replacing each ValueSet with one that points at the current View.
    return Iterables.filter(Iterables.transform(super.getValueSets(), getValueSetMappingFunction()), new Predicate<ValueSet>() {

      @Override
      public boolean apply(ValueSet input) {
        return input.getVariableEntity() != null;
      }
    });
  }

  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
    if(unmapped == null) throw new NoSuchValueSetException(this, entity);
    return getValueSetMappingFunction().apply(super.getValueSet(unmapped));
  }

  //
  // TransformingValueTable Methods
  //

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

  public BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction() {
    return new BijectiveFunction<VariableValueSource, VariableValueSource>() {
      public VariableValueSource apply(VariableValueSource from) {
        return new VariableValueSourceWrapper(from);
      }

      @Override
      public VariableValueSource unapply(VariableValueSource from) {
        return ((VariableValueSourceWrapper) from).wrapped;
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
      return wrapped.getVariable();
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
