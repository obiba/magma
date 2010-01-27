package org.obiba.magma.views;

import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.AbstractValueTableWrapper;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.views.support.AllClause;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class View extends AbstractValueTableWrapper {
  //
  // Instance Variables
  //

  private String name;

  private ValueTable from;

  private SelectClause selectClause;

  private WhereClause whereClause;

  //
  // Constructors
  //

  public View(String name, ValueTable from, SelectClause selectClause, WhereClause whereClause) {
    this.name = name;
    this.from = from;

    setSelectClause(selectClause);
    setWhereClause(whereClause);
  }

  public View(String name, ValueTable from) {
    this(name, from, new AllClause(), new AllClause());
  }

  //
  // AbstractValueTableWrapper Methods
  //

  @Override
  public ValueTable getWrappedValueTable() {
    return from;
  }

  public boolean hasValueSet(VariableEntity entity) {
    boolean hasValueSet = super.hasValueSet(entity);
    if(hasValueSet) {
      ValueSet valueSet = super.getValueSet(entity);
      hasValueSet = whereClause.where(valueSet);
    }
    return hasValueSet;
  }

  public Iterable<ValueSet> getValueSets() {
    // Get a ValueSet Iterable, taking into account the WhereClause.
    Iterable<ValueSet> valueSets = super.getValueSets();
    Iterable<ValueSet> filteredValueSets = Iterables.filter(valueSets, new Predicate<ValueSet>() {
      public boolean apply(ValueSet input) {
        return whereClause.where(input);
      }
    });

    // Transform the Iterable, replacing each ValueSet with one that points at the current View.
    Iterable<ValueSet> viewValueSets = Iterables.transform(filteredValueSets, getValueSetTransformer());

    return viewValueSets;
  }

  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    ValueSet valueSet = super.getValueSet(entity);
    if(!whereClause.where(valueSet)) {
      throw new NoSuchValueSetException(this, entity);
    }

    return getValueSetTransformer().apply(valueSet);
  }

  public Iterable<Variable> getVariables() {
    Iterable<Variable> variables = super.getVariables();
    Iterable<Variable> filteredVariables = Iterables.filter(variables, new Predicate<Variable>() {
      public boolean apply(Variable input) {
        return selectClause.select(input);
      }
    });

    return filteredVariables;
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    Variable variable = super.getVariable(name);
    if(selectClause.select(variable)) {
      return variable;
    } else {
      throw new NoSuchVariableException(name);
    }
  }

  public Value getValue(Variable variable, ValueSet valueSet) {
    if(!whereClause.where(valueSet)) {
      throw new NoSuchValueSetException(this, valueSet.getVariableEntity());
    }

    return super.getValue(variable, valueSet);
  }

  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    // Call getVariable(name) to check the SelectClause (if there is one). If the specified variable
    // is not selected by the SelectClause, this will result in a NoSuchVariableException.
    getVariable(name);

    // Variable "survived" the SelectClause. Go ahead and call the base class method.
    return super.getVariableValueSource(name);
  }

  //
  // Methods
  //

  public String getName() {
    return name;
  }

  public void setSelectClause(SelectClause selectClause) {
    if(selectClause == null) {
      throw new IllegalArgumentException("null selectClause");
    }
    this.selectClause = selectClause;
  }

  public void setWhereClause(WhereClause whereClause) {
    if(whereClause == null) {
      throw new IllegalArgumentException("null whereClause");
    }
    this.whereClause = whereClause;
  }

  protected Function<ValueSet, ValueSet> getValueSetTransformer() {
    return new Function<ValueSet, ValueSet>() {
      public ValueSet apply(ValueSet from) {
        return new ValueSetBean(View.this, from.getVariableEntity());
      }
    };
  }

  //
  // Builder
  //

  public static class Builder {

    private View view;

    public Builder(String name, ValueTable from) {
      view = new View(name, from);
    }

    public static Builder newView(String name, ValueTable from) {
      return new Builder(name, from);
    }

    public Builder select(SelectClause selectClause) {
      view.setSelectClause(selectClause);
      return this;
    }

    public Builder where(WhereClause whereClause) {
      view.setWhereClause(whereClause);
      return this;
    }

    public View build() {
      return view;
    }
  }
}
