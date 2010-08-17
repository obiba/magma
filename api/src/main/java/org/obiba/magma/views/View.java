package org.obiba.magma.views;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
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
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.AbstractValueTableWrapper;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.transform.BijectiveFunctions;
import org.obiba.magma.transform.TransformingValueTable;
import org.obiba.magma.views.support.AllClause;
import org.obiba.magma.views.support.NoneClause;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class View extends AbstractValueTableWrapper implements Initialisable, Disposable, TransformingValueTable {
  //
  // Instance Variables
  //

  private String name;

  private ValueTable from;

  private SelectClause select;

  private WhereClause where;

  /** A list of derived variables. Mutually exclusive with "select". */
  private ListClause variables;

  private transient ViewAwareDatasource viewDatasource;

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

  @Override
  public void dispose() {
    Disposables.silentlyDispose(from, select, where, variables);
  }

  public ListClause getListClause() {
    return variables;
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
    VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
    if(unmapped == null) return false;

    boolean hasValueSet = super.hasValueSet(unmapped);
    if(hasValueSet) {
      ValueSet valueSet = super.getValueSet(unmapped);
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
    return Iterables.transform(filteredValueSets, getValueSetMappingFunction());
  }

  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
    if(unmapped == null) throw new NoSuchValueSetException(this, entity);

    ValueSet valueSet = super.getValueSet(unmapped);
    if(!where.where(valueSet)) {
      throw new NoSuchValueSetException(this, entity);
    }

    return getValueSetMappingFunction().apply(valueSet);
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
        return super.getValue(variable, getValueSetMappingFunction().unapply(valueSet));
      } else {
        return getListClauseValue(variable, valueSet);
      }
    }
    if(!where.where(valueSet)) {
      throw new NoSuchValueSetException(this, valueSet.getVariableEntity());
    }

    return super.getValue(variable, getValueSetMappingFunction().unapply(valueSet));
  }

  private boolean isVariableInSuper(Variable variable, ValueSet valueSet) {
    try {
      super.getValue(variable, getValueSetMappingFunction().unapply(valueSet));
      return true;
    } catch(NoSuchVariableException e) {
      return false;
    }
  }

  private Value getListClauseValue(Variable variable, ValueSet valueSet) {
    return getListClauseVariableValueSource(variable.getName()).getValue(valueSet);
  }

  private VariableValueSource getListClauseVariableValueSource(String name) {
    return getVariableValueSourceMappingFunction().apply(variables.getVariableValueSource(name));
  }

  @Override
  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    if(isViewOfDerivedVariables()) {
      if(isVariableValueSourceInSuper(name)) {
        return getVariableValueSourceMappingFunction().apply(super.getVariableValueSource(name));
      } else {
        return getListClauseVariableValueSource(name);
      }
    }

    // Call getVariable(name) to check the SelectClause (if there is one). If the specified variable
    // is not selected by the SelectClause, this will result in a NoSuchVariableException.
    getVariable(name);

    // Variable "survived" the SelectClause. Go ahead and call the base class method.
    return getVariableValueSourceMappingFunction().apply(super.getVariableValueSource(name));
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

    // First, we transform super.getVariableEntities() using getVariableEntityMappingFunction() (which may modified
    // entity identifiers)
    // Second, we filter the resulting entities to remove the ones for which hasValueSet() is false (usually due to a
    // where clause)
    // Third, we construct an ImmutableSet from the result

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
        return new ValueSetWrapper(View.this, from);
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

  class VariableValueSourceWrapper implements VariableValueSource {

    private final VariableValueSource wrapped;

    VariableValueSourceWrapper(VariableValueSource wrapped) {
      this.wrapped = wrapped;
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
