package org.obiba.magma.filter;

import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractValueTableWrapper;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class FilteredValueTable extends AbstractValueTableWrapper {

  private FilterChain<ValueSet> entityFilterChain;

  private FilterChain<Variable> variableFilterChain;

  private ValueTable valueTable;

  public FilteredValueTable(ValueTable valueTable, FilterChain<Variable> variableFilterChain, FilterChain<ValueSet> entityFilterChain) {
    this.valueTable = valueTable;
    this.entityFilterChain = entityFilterChain;
    this.variableFilterChain = variableFilterChain;
  }

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
    return Iterables.filter(getWrappedValueTable().getValueSets(), new Predicate<ValueSet>() {
      @Override
      public boolean apply(ValueSet input) {
        return entityFilterChain.filter(input) != null;
      }
    });
  }

  @Override
  public Iterable<Variable> getVariables() {
    return Iterables.filter(getWrappedValueTable().getVariables(), new Predicate<Variable>() {
      @Override
      public boolean apply(Variable input) {
        return variableFilterChain.filter(input) != null;
      }
    });
  }

}
