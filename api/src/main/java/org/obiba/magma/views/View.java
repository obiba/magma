package org.obiba.magma.views;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.AbstractValueTableWrapper;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.views.support.AllClause;
import org.obiba.magma.views.support.NoneClause;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class View extends AbstractValueTableWrapper implements Initialisable {
  //
  // Instance Variables
  //

  private ViewAwareDatasource viewDatasource;

  private String name;

  private ValueTable from;

  private SelectClause select;

  private WhereClause where;

  /** A list of derived variables. Mutually exclusive with "select". */
  private ListClause variables;

  //
  // Constructors
  //

  /**
   * No-arg constructor for XStream.
   */
  public View() {
    setSelectClause(new AllClause());
    setWhereClause(new AllClause());
    setListClause(new NoneClause());
  }

  public View(String name, SelectClause selectClause, WhereClause whereClause, ValueTable... from) {
    this.name = name;

    if(from == null || from.length == 0) {
      throw new IllegalArgumentException("null or empty table list");
    }

    if(from.length > 1) {
      this.from = new JoinTable(Arrays.asList(from));
    } else {
      this.from = from[0];
    }

    setSelectClause(selectClause);
    setWhereClause(whereClause);
    setListClause(new NoneClause());
  }

  public View(String name, ValueTable... from) {
    this(name, new AllClause(), new AllClause(), from);
  }

  //
  // Initialisable Methods
  //

  public void initialise() {
    variables.setValueTable(from);
    Initialisables.initialise(select, where, variables);
    if(isViewOfDerivedVariables()) {
      setSelectClause(new NoneClause());
    } else if(select != null && !(select instanceof NoneClause)) {
      setListClause(new NoneClause());
    } else {
      setListClause(new NoneClause());
      setSelectClause(new AllClause());
    }
  }

  /**
   * Returns true is this is a {@link View} of derived variables, false if this is a {@code View} of selected (existing)
   * variables.
   */
  private boolean isViewOfDerivedVariables() {
    return !(variables instanceof NoneClause);
  }

  //
  // AbstractValueTableWrapper Methods
  //

  @Override
  public Datasource getDatasource() {
    return viewDatasource != null ? viewDatasource : getWrappedValueTable().getDatasource();
  }

  public ValueTable getWrappedValueTable() {
    return from;
  }

  public boolean hasValueSet(VariableEntity entity) {
    boolean hasValueSet = super.hasValueSet(entity);
    if(hasValueSet) {
      ValueSet valueSet = super.getValueSet(entity);
      hasValueSet = where.where(valueSet);
    }
    return hasValueSet;
  }

  public Iterable<ValueSet> getValueSets() {
    // Get a ValueSet Iterable, taking into account the WhereClause.
    Iterable<ValueSet> valueSets = super.getValueSets();
    Iterable<ValueSet> filteredValueSets = Iterables.filter(valueSets, new Predicate<ValueSet>() {
      public boolean apply(ValueSet input) {
        return where.where(input);
      }
    });

    // Transform the Iterable, replacing each ValueSet with one that points at the current View.
    Iterable<ValueSet> viewValueSets = Iterables.transform(filteredValueSets, getValueSetTransformer());
    return viewValueSets;
  }

  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    ValueSet valueSet = super.getValueSet(entity);
    if(!where.where(valueSet)) {
      throw new NoSuchValueSetException(this, entity);
    }

    return getValueSetTransformer().apply(valueSet);
  }

  public Iterable<Variable> getVariables() {
    if(isViewOfDerivedVariables()) return getListVariables();
    return getWhereVariables();
  }

  private Iterable<Variable> getWhereVariables() {
    Iterable<Variable> variables = super.getVariables();
    Iterable<Variable> filteredVariables = Iterables.filter(variables, new Predicate<Variable>() {
      public boolean apply(Variable input) {
        return select.select(input);
      }
    });
    return filteredVariables;
  }

  private Iterable<Variable> getListVariables() {
    Set<Variable> variables = new HashSet<Variable>();
    for(VariableValueSource variableValueSource : this.variables.getVariableValueSources()) {
      variables.add(variableValueSource.getVariable());
    }
    return variables;
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    if(isViewOfDerivedVariables()) return getListVariable(name);
    return getWhereVariable(name);
  }

  private Variable getWhereVariable(String name) throws NoSuchVariableException {
    Variable variable = super.getVariable(name);
    if(select.select(variable)) {
      return variable;
    } else {
      throw new NoSuchVariableException(name);
    }
  }

  private Variable getListVariable(String name) throws NoSuchVariableException {
    return variables.getVariableValueSource(name).getVariable();
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    if(isViewOfDerivedVariables()) {
      if(where.where(valueSet) && isVariableInSuper(variable, valueSet)) {
        return super.getValue(variable, ((ValueSetWrapper) valueSet).getWrappedValueSet());
      } else {
        return getListValue(variable, valueSet);
      }
    }
    if(!where.where(valueSet)) {
      throw new NoSuchValueSetException(this, valueSet.getVariableEntity());
    }

    return super.getValue(variable, ((ValueSetWrapper) valueSet).getWrappedValueSet());
  }

  private boolean isVariableInSuper(Variable variable, ValueSet valueSet) {
    try {
      super.getValue(variable, ((ValueSetWrapper) valueSet).getWrappedValueSet());
      return true;
    } catch(NoSuchVariableException e) {
      return false;
    }
  }

  private Value getListValue(Variable variable, ValueSet valueSet) {
    VariableValueSource variableValueSource = variables.getVariableValueSource(variable.getName());
    return variableValueSource.getValue(valueSet);
  }

  @Override
  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    if(isViewOfDerivedVariables()) {
      if(isVariableValueSourceInSuper(name)) {
        return getVariableValueSourceTransformer().apply(super.getVariableValueSource(name));
      } else {
        return getVariableValueSourceTransformer().apply(variables.getVariableValueSource(name));
      }
    }

    // Call getVariable(name) to check the SelectClause (if there is one). If the specified variable
    // is not selected by the SelectClause, this will result in a NoSuchVariableException.
    getVariable(name);

    // Variable "survived" the SelectClause. Go ahead and call the base class method.
    return getVariableValueSourceTransformer().apply(super.getVariableValueSource(name));
  }

  private boolean isVariableValueSourceInSuper(String name) {
    try {
      super.getVariableValueSource(name);
      return true;
    } catch(NoSuchVariableException e) {
      return false;
    }
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    Set<VariableEntity> viewEntities = new HashSet<VariableEntity>();
    for(VariableEntity entity : super.getVariableEntities()) {
      viewEntities.add(getVariableEntityTransformer().apply(entity));
    }
    return viewEntities;
  }

  @Override
  public Timestamps getTimestamps(ValueSet valueSet) {
    return super.getTimestamps(((ValueSetWrapper) valueSet).getWrappedValueSet());
  }

  //
  // Methods
  //

  public void setDatasource(ViewAwareDatasource datasource) {
    this.viewDatasource = datasource;
  }

  public String getName() {
    return name;
  }

  public void setSelectClause(SelectClause selectClause) {
    if(selectClause == null) {
      throw new IllegalArgumentException("null selectClause");
    }
    this.select = selectClause;
  }

  public void setWhereClause(WhereClause whereClause) {
    if(whereClause == null) {
      throw new IllegalArgumentException("null whereClause");
    }
    this.where = whereClause;
  }

  public void setListClause(ListClause listClause) {
    if(listClause == null) {
      throw new IllegalArgumentException("null listClause");
    }
    this.variables = listClause;
  }

  public Function<VariableEntity, VariableEntity> getVariableEntityTransformer() {
    return new Function<VariableEntity, VariableEntity>() {
      public VariableEntity apply(VariableEntity from) {
        return from;
      }
    };
  }

  public Function<ValueSet, ValueSet> getValueSetTransformer() {
    return new Function<ValueSet, ValueSet>() {
      public ValueSet apply(ValueSet from) {
        return new ValueSetWrapper(View.this, from);
      }
    };
  }

  public Function<VariableValueSource, VariableValueSource> getVariableValueSourceTransformer() {
    return new Function<VariableValueSource, VariableValueSource>() {
      public VariableValueSource apply(VariableValueSource from) {
        return new VariableValueSourceWrapper(from);
      }
    };
  }

  static class VariableValueSourceWrapper implements VariableValueSource {
    private VariableValueSource wrapped;

    VariableValueSourceWrapper(VariableValueSource wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    public Variable getVariable() {
      return wrapped.getVariable();
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      return wrapped.getValue(((ValueSetWrapper) valueSet).getWrappedValueSet());
    }

    @Override
    public ValueType getValueType() {
      return wrapped.getValueType();
    }
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

    public Builder cacheWhere() {
      if(view.where == null) throw new IllegalStateException("where clause not specified");
      view.setWhereClause(new CachingWhereClause(view.where));
      return this;
    }

    public View build() {
      return view;
    }

    public Builder list(ListClause listClause) {
      view.setListClause(listClause);
      return this;
    }
  }

}
