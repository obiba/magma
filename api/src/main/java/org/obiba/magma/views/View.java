package org.obiba.magma.views;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
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

  @Nonnull
  private ValueTable from;

  @Nonnull
  private SelectClause select;

  @Nonnull
  private WhereClause where;

  /**
   * A list of derived variables. Mutually exclusive with "select".
   */
  @Nonnull
  private ListClause variables;

  private Value created;

  private Value updated;

  // need to be transient because of XML serialization of Views
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  @Nullable
  private transient ViewAwareDatasource viewDatasource;

  /**
   * No-arg constructor for XStream.
   */
  @SuppressWarnings("PMD.NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
  public View() {
    setSelectClause(new AllClause());
    setWhereClause(new AllClause());
    setListClause(new NoneClause());
  }

  @SuppressWarnings({ "ConstantConditions", "PMD.NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" })
  public View(String name, @Nonnull SelectClause selectClause, @Nonnull WhereClause whereClause,
      @Nonnull ValueTable... from) {
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
    getListClause().setValueTable(getWrappedValueTable());
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

  @Nonnull
  public SelectClause getSelectClause() {
    return select;
  }

  @Nonnull
  public WhereClause getWhereClause() {
    return where;
  }

  @Nonnull
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

  @Override
  public Datasource getDatasource() {
    return viewDatasource == null ? getWrappedValueTable().getDatasource() : viewDatasource;
  }

  @Override
  @Nonnull
  public ValueTable getWrappedValueTable() {
    return from;
  }

  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {

      @Override
      public Value getLastUpdate() {
        if(updated == null || updated.isNull()) {
          return getWrappedValueTable().getTimestamps().getLastUpdate();
        }
        Value fromUpdate = getWrappedValueTable().getTimestamps().getLastUpdate();
        return !fromUpdate.isNull() && updated.compareTo(fromUpdate) < 0 ? fromUpdate : updated;
      }

      @Override
      public Value getCreated() {
        return created == null || created.isNull() ? getWrappedValueTable().getTimestamps().getCreated() : created;
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
    Collection<Variable> listVariables = new LinkedHashSet<Variable>();
    for(VariableValueSource variableValueSource : getListClause().getVariableValueSources()) {
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
    return getVariableValueSourceMappingFunction().apply(getListClause().getVariableValueSource(variableName));
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
    // do not use Guava functional stuff to avoid multiple iterations over entities
    Set<VariableEntity> entities = Sets.newLinkedHashSet();
    for(VariableEntity entity : super.getVariableEntities()) {
      // transform super.getVariableEntities() using getVariableEntityMappingFunction()
      // (which may modified entity identifiers)
      entity = getVariableEntityMappingFunction().apply(entity);

      // filter the resulting entities to remove the ones for which hasValueSet() is false
      // (usually due to a where clause)
      if(hasValueSet(entity)) {
        entities.add(entity);
      }
    }
    return ImmutableSet.copyOf(entities);
  }

  public void setDatasource(ViewAwareDatasource datasource) {
    viewDatasource = datasource;
  }

  @Override
  public String getName() {
    return name;
  }

  @SuppressWarnings("ConstantConditions")
  public void setSelectClause(@Nonnull SelectClause selectClause) {
    Preconditions.checkArgument(selectClause != null, "null selectClause");
    select = selectClause;
  }

  @SuppressWarnings("ConstantConditions")
  public void setWhereClause(@Nonnull WhereClause whereClause) {
    Preconditions.checkArgument(whereClause != null, "null whereClause");
    where = whereClause;
  }

  @SuppressWarnings("ConstantConditions")
  public void setListClause(@Nonnull ListClause listClause) {
    Preconditions.checkArgument(listClause != null, "null listClause");
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
      public ValueSet unapply(@SuppressWarnings("ParameterHidesMemberVariable") ValueSet from) {
        return ((ValueSetWrapper) from).getWrappedValueSet();
      }

      @Override
      public ValueSet apply(@SuppressWarnings("ParameterHidesMemberVariable") ValueSet from) {
        return new ValueSetWrapper(View.this, from);
      }
    };
  }

  @Override
  public BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction() {
    return new BijectiveFunction<VariableValueSource, VariableValueSource>() {
      @Override
      public VariableValueSource apply(@SuppressWarnings("ParameterHidesMemberVariable") VariableValueSource from) {
        return new ViewVariableValueSource(from);
      }

      @Override
      public VariableValueSource unapply(@SuppressWarnings("ParameterHidesMemberVariable") VariableValueSource from) {
        return ((AbstractVariableValueSourceWrapper) from).getWrapped();
      }
    };
  }

  protected class ViewVariableValueSource extends AbstractVariableValueSourceWrapper {

    public ViewVariableValueSource(VariableValueSource wrapped) {
      super(wrapped);
    }

    @Nonnull
    @Override
    public Value getValue(ValueSet valueSet) {
      return getWrapped().getValue(getValueSetMappingFunction().unapply(valueSet));
    }

  }

  //
  // Builder
  //

  @SuppressWarnings({ "UnusedDeclaration", "StaticMethodOnlyUsedInOneClass" })
  public static class Builder {

    private final View view;

    public Builder(String name, @Nonnull ValueTable... from) {
      view = new View(name, from);
    }

    public static Builder newView(String name, @Nonnull ValueTable... from) {
      return new Builder(name, from);
    }

    public Builder select(@Nonnull SelectClause selectClause) {
      view.setSelectClause(selectClause);
      return this;
    }

    public Builder where(@Nonnull WhereClause whereClause) {
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
