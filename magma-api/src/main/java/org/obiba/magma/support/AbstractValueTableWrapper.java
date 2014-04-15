package org.obiba.magma.support;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaCacheExtension;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.springframework.cache.Cache;

public abstract class AbstractValueTableWrapper implements ValueTableWrapper {


  @Nullable
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient VariableEntitiesCache variableEntitiesCache;

  @Override
  public abstract ValueTable getWrappedValueTable();

  @NotNull
  @Override
  public Datasource getDatasource() {
    return getWrappedValueTable().getDatasource();
  }

  @Override
  public String getEntityType() {
    return getWrappedValueTable().getEntityType();
  }

  @NotNull
  @Override
  public String getName() {
    return getWrappedValueTable().getName();
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    return getWrappedValueTable().getValue(variable, valueSet);
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    Value tableWrapperLastUpdate = getTimestamps().getLastUpdate();
    VariableEntitiesCache eCache = getVariableEntitiesCache();
    if(eCache == null || !eCache.isUpToDate(tableWrapperLastUpdate)) {
      eCache = new VariableEntitiesCache(loadVariableEntities(), tableWrapperLastUpdate);
      if(MagmaEngine.get().hasExtension(MagmaCacheExtension.class)) {
        MagmaCacheExtension cacheExtension = MagmaEngine.get().getExtension(MagmaCacheExtension.class);
        if (cacheExtension.hasVariableEntitiesCache()) {
          cacheExtension.getVariableEntitiesCache().put(getTableReference(), eCache);
        } else {
          variableEntitiesCache = eCache;
        }
      } else {
        variableEntitiesCache = eCache;
      }
    }
    return eCache.getEntities();
  }

  private VariableEntitiesCache getVariableEntitiesCache() {
    if(MagmaEngine.get().hasExtension(MagmaCacheExtension.class)) {
      MagmaCacheExtension cacheExtension = MagmaEngine.get().getExtension(MagmaCacheExtension.class);
      if (!cacheExtension.hasVariableEntitiesCache()) return variableEntitiesCache;
      Cache.ValueWrapper wrapper = cacheExtension.getVariableEntitiesCache().get(getTableReference());
      return wrapper == null ? variableEntitiesCache : (VariableEntitiesCache) wrapper.get();
    } else {
      return variableEntitiesCache;
    }
  }

  protected Set<VariableEntity> loadVariableEntities() {
    return getWrappedValueTable().getVariableEntities();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return getWrappedValueTable().getValueSet(entity);
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return getWrappedValueTable().getValueSetTimestamps(entity);
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return getWrappedValueTable().getValueSets();
  }

  @Override
  public boolean hasVariable(String name) {
    return getWrappedValueTable().hasVariable(name);
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    return getWrappedValueTable().getVariable(name);
  }

  @Override
  public Iterable<Variable> getVariables() {
    return getWrappedValueTable().getVariables();
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    return getWrappedValueTable().getVariableValueSource(variableName);
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    return getWrappedValueTable().hasValueSet(entity);
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return getWrappedValueTable().isForEntityType(entityType);
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return getWrappedValueTable().getTimestamps();
  }

  @Override
  public boolean isView() {
    return getWrappedValueTable().isView();
  }

  @Override
  public ValueTable getInnermostWrappedValueTable() {
    return getWrappedValueTable() instanceof ValueTableWrapper //
        ? ((ValueTableWrapper) getWrappedValueTable()).getInnermostWrappedValueTable() //
        : getWrappedValueTable();
  }

  @Override
  public String getTableReference() {
    return getWrappedValueTable().getTableReference();
  }

  @Override
  public int getVariableCount() {
    return getWrappedValueTable().getVariableCount();
  }

  @Override
  public int getValueSetCount() {
    return getWrappedValueTable().getValueSetCount();
  }

  @Override
  public int getVariableEntityCount() {
    return getWrappedValueTable().getVariableEntityCount();
  }

  //
  // Cache
  //
  public static class VariableEntitiesCache implements Serializable {

    private static final long serialVersionUID = 69918333951801112L;

    private Set<VariableEntity> entities;

    private long lastUpdate;

    public VariableEntitiesCache(Set<VariableEntity> entities, Value lastUpdate) {
      this(entities, ((Date)lastUpdate.getValue()).getTime());
    }

    public VariableEntitiesCache(Set<VariableEntity> entities, long lastUpdate) {
      this.entities = entities;
      this.lastUpdate = lastUpdate;
    }

    public boolean isUpToDate(Value updated) {
      return lastUpdate == ((Date)updated.getValue()).getTime();
    }

    public Set<VariableEntity> getEntities() {
      return entities;
    }
  }
}
