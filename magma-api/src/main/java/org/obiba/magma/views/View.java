package org.obiba.magma.views;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

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
import org.obiba.magma.VariableValueSourceWrapper;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.AbstractValueTableWrapper;
import org.obiba.magma.support.AbstractVariableValueSourceWrapper;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.transform.BijectiveFunctions;
import org.obiba.magma.transform.TransformingValueTable;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.views.support.AllClause;
import org.obiba.magma.views.support.NoneClause;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("OverlyCoupledClass")
public class View extends AbstractValueTableWrapper implements Initialisable, Disposable, TransformingValueTable {

//  private static final Logger log = LoggerFactory.getLogger(View.class);

  private String name;

  @NotNull
  private ValueTable from;

  @NotNull
  private SelectClause select;

  @NotNull
  private WhereClause where;

  /**
   * A list of derived variables. Mutually exclusive with "select".
   */
  @NotNull
  private ListClause variables;

  private Value created;

  private Value updated;

  // need to be transient because of XML serialization of Views
  @Nullable
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient ViewAwareDatasource viewDatasource;

  /**
   * No-arg constructor for XStream.
   */
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
      justification = "Needed by XStream")
  public View() {
    setSelectClause(new AllClause());
    setWhereClause(new AllClause());
    setListClause(new NoneClause());
  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
      justification = "Needed by XStream")
  public View(String name, @NotNull SelectClause selectClause, @NotNull WhereClause whereClause,
      @NotNull ValueTable... from) {
    Preconditions.checkArgument(selectClause != null, "null selectClause");
    Preconditions.checkArgument(whereClause != null, "null whereClause");
    Preconditions.checkArgument(from != null && from.length > 0, "null or empty table list");
    this.name = name;
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

  @Override
  public void initialise() {
    getListClause().setValueTable(this);
    Initialisables.initialise(getWrappedValueTable(), getSelectClause(), getWhereClause(), getListClause());
    if(isViewOfDerivedVariables()) {
      setSelectClause(new NoneClause());
    } else if(!(getSelectClause() instanceof NoneClause)) {
      setListClause(new NoneClause());
    } else {
      setListClause(new NoneClause());
      setSelectClause(new AllClause());
    }
  }

  @Override
  public void dispose() {
    Disposables.silentlyDispose(getWrappedValueTable(), getSelectClause(), getWhereClause(), getListClause());
  }

  public void setName(String name) {
    this.name = name;
  }

  @NotNull
  public SelectClause getSelectClause() {
    return select;
  }

  @NotNull
  public WhereClause getWhereClause() {
    return where;
  }

  @NotNull
  public ListClause getListClause() {
    return variables;
  }

  /**
   * Returns true is this is a {@link View} of derived variables, false if this is a {@code View} of selected (existing)
   * variables.
   */
  private boolean isViewOfDerivedVariables() {
    return !(getListClause() instanceof NoneClause);
  }

  @NotNull
  @Override
  public Datasource getDatasource() {
    return viewDatasource == null ? getWrappedValueTable().getDatasource() : viewDatasource;
  }

  @Override
  @NotNull
  public ValueTable getWrappedValueTable() {
    return from;
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {

      @NotNull
      @Override
      public Value getLastUpdate() {
        if(updated == null || updated.isNull()) {
          return getWrappedValueTable().getTimestamps().getLastUpdate();
        }
        Value fromUpdate = getWrappedValueTable().getTimestamps().getLastUpdate();
        return !fromUpdate.isNull() && updated.compareTo(fromUpdate) < 0 ? fromUpdate : updated;
      }

      @NotNull
      @Override
      public Value getCreated() {
        return created == null || created.isNull() ? getWrappedValueTable().getTimestamps().getCreated() : created;
      }
    };
  }

  @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters" })
  public void setUpdated(@Nullable Value updated) {
    if(updated == null) updated = DateTimeType.get().nullValue();
    if(updated.getValueType() != DateTimeType.get()) throw new IllegalArgumentException();
    this.updated = updated;
  }

  @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters" })
  public void setCreated(@Nullable Value created) {
    if(created == null) created = DateTimeType.get().nullValue();
    if(created.getValueType() != DateTimeType.get()) throw new IllegalArgumentException();
    this.created = created;
  }

  @Override
  public boolean isView() {
    return true;
  }

  @Override
  public String getTableReference() {
    return (getDatasource() == null ? "null" : getDatasource().getName()) + "." + getName();
  }

  @Override
  public int getVariableCount() {
    return Iterables.size(getVariables());
  }

  @Override
  public int getValueSetCount() {
    return getVariableEntityCount();
  }

  @Override
  public int getVariableEntityCount() {
    return Iterables.size(getVariableEntities());
  }

  @Override
  @SuppressWarnings("ChainOfInstanceofChecks")
  public boolean hasValueSet(@Nullable VariableEntity entity) {
    if(entity == null) return false;

    VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
    if(unmapped == null) return false;

    boolean hasValueSet = super.hasValueSet(unmapped);

    if(hasValueSet) {
      // Shortcut some WhereClause to prevent loading the ValueSet which may be expensive
      if(getWhereClause() instanceof AllClause) return true;
      if(getWhereClause() instanceof NoneClause) return false;

      ValueSet valueSet = super.getValueSet(unmapped);
      hasValueSet = getWhereClause().where(valueSet);
    }
    return hasValueSet;
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    // do not use Guava functional stuff to avoid multiple iterations over valueSets
    List<ValueSet> valueSets = Lists.newArrayList();
    for(ValueSet valueSet : super.getValueSets()) {
      if(getWhereClause().where(valueSet)) { // taking into account the WhereClause
        // replacing each ValueSet with one that points at the current View
        valueSet = getValueSetMappingFunction().apply(valueSet);
        // result of transformation might have returned a non-mappable entity
        if(valueSet != null && valueSet.getVariableEntity() != null) {
          valueSets.add(valueSet);
        }
      }
    }
    return valueSets;
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    VariableEntity unmapped = getVariableEntityMappingFunction().unapply(entity);
    if(unmapped == null) throw new NoSuchValueSetException(this, entity);

    ValueSet valueSet = super.getValueSet(unmapped);
    if(!getWhereClause().where(valueSet)) throw new NoSuchValueSetException(this, entity);

    return getValueSetMappingFunction().apply(valueSet);
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return getValueSet(entity).getTimestamps();
  }

  @Override
  public Iterable<Variable> getVariables() {
    if(from instanceof JoinTable) {
      ((JoinTable) from).analyseVariables();
    }
    return isViewOfDerivedVariables() ? getListVariables() : getSelectVariables();
  }

  private Iterable<Variable> getSelectVariables() {
    return Iterables.filter(super.getVariables(), new Predicate<Variable>() {
      @Override
      public boolean apply(Variable input) {
        return getSelectClause().select(input);
      }
    });
  }

  private Iterable<Variable> getListVariables() {
    Collection<Variable> listVariables = new LinkedHashSet<>();
    for(VariableValueSource variableValueSource : getListClause().getVariableValueSources()) {
      listVariables.add(variableValueSource.getVariable());
    }
    return listVariables;
  }

  @Override
  public boolean hasVariable(@SuppressWarnings("ParameterHidesMemberVariable") String name) {
    try {
      getVariable(name);
      return true;
    } catch(NoSuchVariableException e) {
      return false;
    }
  }

  @Override
  public Variable getVariable(String variableName) throws NoSuchVariableException {
    return isViewOfDerivedVariables() //
        ? getListVariable(variableName) //
        : getSelectVariable(variableName);
  }

  private Variable getSelectVariable(String variableName) throws NoSuchVariableException {
    Variable variable = super.getVariable(variableName);
    if(getSelectClause().select(variable)) {
      return variable;
    }
    throw new NoSuchVariableException(variableName);
  }

  private Variable getListVariable(String variableName) throws NoSuchVariableException {
    return getListClause().getVariableValueSource(variableName).getVariable();
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    if(isViewOfDerivedVariables()) {
      return getListClauseValue(variable, valueSet);
    }
    if(!getWhereClause().where(valueSet)) {
      throw new NoSuchValueSetException(this, valueSet.getVariableEntity());
    }
    return super.getValue(variable, getValueSetMappingFunction().unapply(valueSet));
  }

  private Value getListClauseValue(Variable variable, ValueSet valueSet) {
    return getListClauseVariableValueSource(variable.getName()).getValue(valueSet);
  }

  private VariableValueSource getListClauseVariableValueSource(String variableName) {
    VariableValueSource variableValueSource = getListClause().getVariableValueSource(variableName);
    return getVariableValueSourceMappingFunction().apply(variableValueSource);
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
  protected Set<VariableEntity> loadVariableEntities() {
    // do not use Guava functional stuff to avoid multiple iterations over entities
    ImmutableSet.Builder<VariableEntity> builder = ImmutableSet.builder();
    if(hasVariables()) {
      for(VariableEntity entity : super.loadVariableEntities()) {
        // transform super.getVariableEntities() using getVariableEntityMappingFunction()
        // (which may modified entity identifiers)
        entity = getVariableEntityMappingFunction().apply(entity);

        // filter the resulting entities to remove the ones for which hasValueSet() is false
        // (usually due to a where clause)
        if(hasValueSet(entity)) {
          builder.add(entity);
        }
      }
    }
    return builder.build();
  }

  public void setDatasource(ViewAwareDatasource datasource) {
    viewDatasource = datasource;
  }

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  @SuppressWarnings("ConstantConditions")
  public void setSelectClause(@NotNull SelectClause selectClause) {
    Preconditions.checkArgument(selectClause != null, "null selectClause");
    select = selectClause;
  }

  @SuppressWarnings("ConstantConditions")
  public void setWhereClause(@NotNull WhereClause whereClause) {
    Preconditions.checkArgument(whereClause != null, "null whereClause");
    where = whereClause;
  }

  @SuppressWarnings("ConstantConditions")
  public void setListClause(@NotNull ListClause listClause) {
    Preconditions.checkArgument(listClause != null, "null listClause");
    variables = listClause;
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
      public ValueSet unapply(@SuppressWarnings("ParameterHidesMemberVariable") ValueSet from) {
        return ((ValueSetWrapper) from).getWrappedValueSet();
      }

      @Override
      public ValueSet apply(@SuppressWarnings("ParameterHidesMemberVariable") ValueSet from) {
        return new ValueSetWrapper(View.this, from);
      }
    };
  }

  @NotNull
  @Override
  public BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction() {
    return new BijectiveFunction<VariableValueSource, VariableValueSource>() {
      @Override
      public VariableValueSource apply(@SuppressWarnings("ParameterHidesMemberVariable") VariableValueSource from) {
        return new ViewVariableValueSource(from);
      }

      @Override
      public VariableValueSource unapply(@SuppressWarnings("ParameterHidesMemberVariable") VariableValueSource from) {
        return ((VariableValueSourceWrapper) from).getWrapped();
      }
    };
  }

  private boolean hasVariables() {
    return !(select instanceof NoneClause) || variables.getVariableValueSources().iterator().hasNext();
  }

  protected class ViewVariableValueSource extends AbstractVariableValueSourceWrapper {

    public ViewVariableValueSource(VariableValueSource wrapped) {
      super(wrapped);
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      return getWrapped().getValue(getValueSetMappingFunction().unapply(valueSet));
    }

    @NotNull
    @Override
    public VectorSource asVectorSource() {
      return new ViewVectorSource(super.asVectorSource());
    }

    private class ViewVectorSource implements VectorSource {

      private final VectorSource wrapped;

      private SortedSet<VariableEntity> mappedEntities;

      private ViewVectorSource(VectorSource wrapped) {
        this.wrapped = wrapped;
      }

      @Override
      public ValueType getValueType() {
        return wrapped.getValueType();
      }

      @Override
      public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
        return wrapped.getValues(getMappedEntities(entities));
      }

      private SortedSet<VariableEntity> getMappedEntities(Iterable<VariableEntity> entities) {
        if(mappedEntities == null) {
          mappedEntities = Sets.newTreeSet();
          for(VariableEntity entity : entities) {
            mappedEntities.add(getVariableEntityMappingFunction().unapply(entity));
          }
        }
        return mappedEntities;
      }

      @Override
      @SuppressWarnings("SimplifiableIfStatement")
      public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        return wrapped.equals(((ViewVectorSource) obj).wrapped);
      }

      @Override
      public int hashCode() {
        return wrapped.hashCode();
      }
    }

  }

  //
  // Builder
  //

  @SuppressWarnings({ "UnusedDeclaration", "StaticMethodOnlyUsedInOneClass" })
  public static class Builder {

    private final View view;

    public Builder(String name, @NotNull ValueTable... from) {
      view = new View(name, from);
    }

    public static Builder newView(String name, @NotNull ValueTable... from) {
      return new Builder(name, from);
    }

    public Builder select(@NotNull SelectClause selectClause) {
      view.setSelectClause(selectClause);
      return this;
    }

    public Builder where(@NotNull WhereClause whereClause) {
      view.setWhereClause(whereClause);
      return this;
    }

    public Builder cacheWhere() {
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
