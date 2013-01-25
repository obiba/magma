package org.obiba.magma.views;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
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
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.views.support.AllClause;
import org.obiba.magma.views.support.NoneClause;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@SuppressWarnings({ "ParameterHidesMemberVariable", "OverlyCoupledClass" })
public class View extends AbstractValueTableWrapper implements Initialisable, Disposable, TransformingValueTable {
  //
  // Instance Variables
  //

  private String name;

  private ValueTable from;

  private SelectClause select;

  private WhereClause where;

  /**
   * A list of derived variables. Mutually exclusive with "select".
   */
  private ListClause variables;

  private Value created;

  private Value updated;

  // need to be transient because of XML serialization of Views
  @SuppressWarnings("TransientFieldInNonSerializableClass")
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

    this.from = from.length > 1 ? new JoinTable(Arrays.asList(from)) : from[0];

    setSelectClause(selectClause);
    setWhereClause(whereClause);
    setListClause(new NoneClause());

    created = DateTimeType.get().now();
    updated = DateTimeType.get().now();
  }

  public View(String name, ValueTable... from) {
    this(name, new AllClause(), new AllClause(), from);
  }

  //
  // Initialisable Methods
  //

  @Override
  public void initialise() {
    variables.setValueTable(from);
    Initialisables.initialise(from, select, where, variables);
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

  public SelectClause getSelectClause() {
    return select;
  }

  public WhereClause getWhereClause() {
    return where;
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

  @Override
  public Datasource getDatasource() {
    return viewDatasource != null ? viewDatasource : getWrappedValueTable().getDatasource();
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return from;
  }

  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {

      @Override
      public Value getLastUpdate() {
        if(updated == null || updated.isNull()) {
          return from.getTimestamps().getLastUpdate();
        } else {
          Value fromUpdate = from.getTimestamps().getLastUpdate();
          return !fromUpdate.isNull() && updated.compareTo(fromUpdate) < 0 ? fromUpdate : updated;
        }
      }

      @Override
      public Value getCreated() {
        return created == null || created.isNull() ? from.getTimestamps().getCreated() : created;
      }
    };
  }

  @SuppressWarnings("AssignmentToMethodParameter")
  public void setUpdated(Value updated) {
    if(updated == null) updated = DateTimeType.get().nullValue();
    if(updated.getValueType() != DateTimeType.get()) throw new IllegalArgumentException();
    this.updated = updated;
  }

  @SuppressWarnings("AssignmentToMethodParameter")
  public void setCreated(Value created) {
    if(created == null) created = DateTimeType.get().nullValue();
    if(created.getValueType() != DateTimeType.get()) throw new IllegalArgumentException();
    this.created = created;
  }

  @Override
  public boolean isView() {
    return true;
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    if(entity == null) return false;

    VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
    if(unmapped == null) return false;

    boolean hasValueSet = super.hasValueSet(unmapped);

    if(hasValueSet) {
      // Shortcut some WhereClause to prevent loading the ValueSet which may be expensive
      if(where instanceof AllClause) return true;
      if(where instanceof NoneClause) return false;

      ValueSet valueSet = super.getValueSet(unmapped);
      hasValueSet = where.where(valueSet);
    }
    return hasValueSet;
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    // Get a ValueSet Iterable, taking into account the WhereClause.
    Iterable<ValueSet> valueSets = super.getValueSets();
    Iterable<ValueSet> filteredValueSets = Iterables.filter(valueSets, new Predicate<ValueSet>() {
      @Override
      public boolean apply(ValueSet input) {
        return where.where(input);
      }
    });

    // Transform the Iterable, replacing each ValueSet with one that points at the current View.
    return Iterables
        .filter(Iterables.transform(filteredValueSets, getValueSetMappingFunction()), new Predicate<ValueSet>() {

          @Override
          public boolean apply(ValueSet input) {
            // Result of transformation might have returned a non-mappable entity
            return input.getVariableEntity() != null;
          }

        });
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
    if(unmapped == null) throw new NoSuchValueSetException(this, entity);

    ValueSet valueSet = super.getValueSet(unmapped);
    if(!where.where(valueSet)) {
      throw new NoSuchValueSetException(this, entity);
    }

    return getValueSetMappingFunction().apply(valueSet);
  }

  @Override
  public Iterable<Variable> getVariables() {
    if(isViewOfDerivedVariables()) return getListVariables();
    return getSelectVariables();
  }

  private Iterable<Variable> getSelectVariables() {
    return Iterables.filter(super.getVariables(), new Predicate<Variable>() {
      @Override
      public boolean apply(Variable input) {
        return select.select(input);
      }
    });
  }

  private Iterable<Variable> getListVariables() {
    Collection<Variable> listVariables = new LinkedHashSet<Variable>();
    for(VariableValueSource variableValueSource : variables.getVariableValueSources()) {
      listVariables.add(variableValueSource.getVariable());
    }
    return listVariables;
  }

  @Override
  public Variable getVariable(String variableName) throws NoSuchVariableException {
    if(isViewOfDerivedVariables()) return getListVariable(variableName);
    return getSelectVariable(variableName);
  }

  private Variable getSelectVariable(String variableName) throws NoSuchVariableException {
    Variable variable = super.getVariable(variableName);
    if(select.select(variable)) {
      return variable;
    } else {
      throw new NoSuchVariableException(variableName);
    }
  }

  private Variable getListVariable(String variableName) throws NoSuchVariableException {
    return variables.getVariableValueSource(variableName).getVariable();
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    if(isViewOfDerivedVariables()) {
      return getListClauseValue(variable, valueSet);
    }
    if(!where.where(valueSet)) {
      throw new NoSuchValueSetException(this, valueSet.getVariableEntity());
    }

    return super.getValue(variable, getValueSetMappingFunction().unapply(valueSet));
  }

  private Value getListClauseValue(Variable variable, ValueSet valueSet) {
    return getListClauseVariableValueSource(variable.getName()).getValue(valueSet);
  }

  private VariableValueSource getListClauseVariableValueSource(String variableName) {
    return getVariableValueSourceMappingFunction().apply(variables.getVariableValueSource(variableName));
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    if(isViewOfDerivedVariables()) {
      return getListClauseVariableValueSource(variableName);
    }

    // Call getVariable(variableName) to check the SelectClause (if there is one). If the specified variable
    // is not selected by the SelectClause, this will result in a NoSuchVariableException.
    getVariable(variableName);

    // Variable "survived" the SelectClause. Go ahead and call the base class method.
    return getVariableValueSourceMappingFunction().apply(super.getVariableValueSource(variableName));
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {

    // First, we transform super.getVariableEntities() using getVariableEntityMappingFunction() (which may modified
    // entity identifiers)
    // Second, we filter the resulting entities to remove the ones for which hasValueSet() is false (usually due to a
    // where clause)
    // Third, we construct an ImmutableSet from the result

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

  public void setDatasource(ViewAwareDatasource datasource) {
    viewDatasource = datasource;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setSelectClause(SelectClause selectClause) {
    if(selectClause == null) {
      throw new IllegalArgumentException("null selectClause");
    }
    select = selectClause;
  }

  public void setWhereClause(WhereClause whereClause) {
    if(whereClause == null) {
      throw new IllegalArgumentException("null whereClause");
    }
    where = whereClause;
  }

  public void setListClause(ListClause listClause) {
    if(listClause == null) {
      throw new IllegalArgumentException("null listClause");
    }
    variables = listClause;
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

  @Override
  public BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction() {
    return new BijectiveFunction<VariableValueSource, VariableValueSource>() {
      @Override
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

  //
  // Builder
  //

  public static class Builder {

    private final View view;

    public Builder(String name, ValueTable... from) {
      view = new View(name, from);
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static Builder newView(String name, ValueTable... from) {
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

    @SuppressWarnings("UnusedDeclaration")
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
