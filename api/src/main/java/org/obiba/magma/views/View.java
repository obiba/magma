package org.obiba.magma.views;

import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.AbstractValueTableWrapper;

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

  public View(String name, ValueTable from) {
    this.name = name;
    this.from = from;
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
    if(hasValueSet && whereClause != null) {
      ValueSet valueSet = super.getValueSet(entity);
      hasValueSet = whereClause.where(valueSet);
    }
    return hasValueSet;
  }

  public Iterable<ValueSet> getValueSets() {
    Iterable<ValueSet> valueSets = super.getValueSets();
    if(whereClause != null) {
      List<ValueSet> valueSetsToRetain = new ArrayList<ValueSet>();
      for(ValueSet valueSet : valueSets) {
        if(whereClause.where(valueSet)) {
          valueSetsToRetain.add(valueSet);
        }
      }
      Iterables.retainAll(valueSets, valueSetsToRetain);
    }
    return valueSets;
  }

  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    ValueSet valueSet = super.getValueSet(entity);
    if(whereClause != null) {
      if(whereClause.where(valueSet)) {
        return valueSet;
      } else {
        throw new NoSuchValueSetException(this, entity);
      }
    }
    return valueSet;
  }

  public Iterable<Variable> getVariables() {
    Iterable<Variable> variables = super.getVariables();
    if(selectClause != null) {
      List<Variable> variablesToRetain = new ArrayList<Variable>();
      for(Variable variable : variables) {
        if(selectClause.select(variable)) {
          variablesToRetain.add(variable);
        }
      }
      Iterables.retainAll(variables, variablesToRetain);
    }
    return variables;
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    Variable variable = super.getVariable(name);
    if(selectClause != null) {
      if(selectClause.select(variable)) {
        return variable;
      } else {
        throw new NoSuchVariableException(name);
      }
    }
    return variable;
  }

  public Value getValue(Variable variable, ValueSet valueSet) {
    if(whereClause != null) {
      if(!whereClause.where(valueSet)) {
        throw new NoSuchValueSetException(this, valueSet.getVariableEntity());
      }
    }
    return super.getValue(variable, valueSet);
  }

  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    // Call getVariable(name) to check the SelectClause (if there is one).
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
    this.selectClause = selectClause;
  }

  public void setWhereClause(WhereClause whereClause) {
    this.whereClause = whereClause;
  }

  //
  // Builder
  //

  public static class ViewBuilder {

    private View view;

    public ViewBuilder(String name, ValueTable from) {
      view = new View(name, from);
    }

    public static ViewBuilder newView(String name, ValueTable from) {
      return new ViewBuilder(name, from);
    }

    public ViewBuilder select(SelectClause selectClause) {
      view.setSelectClause(selectClause);
      return this;
    }

    public ViewBuilder where(WhereClause whereClause) {
      view.setWhereClause(whereClause);
      return this;
    }

    public View build() {
      return view;
    }
  }
}
