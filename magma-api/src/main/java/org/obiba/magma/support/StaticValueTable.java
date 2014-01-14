package org.obiba.magma.support;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@SuppressWarnings("UnusedDeclaration")
public class StaticValueTable extends AbstractValueTable {

  private final String entityType;

  private final Set<VariableEntity> entities;

  private final Map<String, Map<String, Object>> table = Maps.newHashMap();

  private String name;

  public StaticValueTable(Datasource datasource, String name, Iterable<String> entities, String entityType) {
    super(datasource, name);

    this.name = name;

    this.entityType = entityType == null ? "" : entityType;

    this.entities = Sets.newLinkedHashSet(Iterables.transform(entities, new Function<String, VariableEntity>() {

      @Override
      public VariableEntity apply(String from) {
        return new VariableEntityBean(StaticValueTable.this.entityType, from);
      }
    }));
    setVariableEntityProvider(new VariableEntityProvider() {

      @Override
      public boolean isForEntityType(String type) {
        return getEntityType().equals(type);
      }

      @Override
      public Set<VariableEntity> getVariableEntities() {
        return ImmutableSet.copyOf(StaticValueTable.this.entities);
      }

      @Override
      public String getEntityType() {
        return StaticValueTable.this.entityType;
      }
    });
  }

  public StaticValueTable(Datasource datasource, String name, Iterable<String> entities) {
    this(datasource, name, entities, "Participant");
  }

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void removeVariable(String variableName) {
    removeVariableValueSource(variableName);
  }

  public void addVariable(final Variable variable) {
    addVariableValueSource(new VariableValueSource() {

      @NotNull
      @Override
      public ValueType getValueType() {
        return variable.getValueType();
      }

      @NotNull
      @Override
      public Value getValue(ValueSet valueSet) {
        Object value = table.get(valueSet.getVariableEntity().getIdentifier()).get(variable.getName());
        return value instanceof Value ? (Value) value : getValueType().valueOf(value);
      }

      @Nullable
      @Override
      public VectorSource asVectorSource() {
        return null;
      }

      @Override
      public Variable getVariable() {
        return variable;
      }
    });
  }

  public void addVariables(final ValueType type, String... variables) {
    for(final String variable : variables) {
      addVariableValueSource(new VariableValueSource() {

        @Override
        public Variable getVariable() {
          return Variable.Builder.newVariable(variable, type, entityType).build();
        }

        @NotNull
        @Override
        public Value getValue(ValueSet valueSet) {
          return type.valueOf(table.get(valueSet.getVariableEntity().getIdentifier()).get(variable));
        }

        @NotNull
        @Override
        public ValueType getValueType() {
          return type;
        }

        @Nullable
        @Override
        public VectorSource asVectorSource() {
          return null;
        }
      });
    }
  }

  public StaticValueTable addValues(String entity, Object... variableAndValues) {
    for(int i = 0; i < variableAndValues.length; i += 2) {
      Object variable = variableAndValues[i];
      if(variable instanceof Variable) {
        Variable var = (Variable) variable;
        if(!hasVariable(var.getName())) {
          addVariable(var);
        }
        variable = var.getName();
      }
      Object value = variableAndValues[i + 1];
      getEntityValues(entity).put(variable.toString(), value);
    }
    return this;
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return NullTimestamps.get();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new ValueSetBean(this, entity);
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return NullTimestamps.get();
  }

  boolean hasVariableEntity(VariableEntity entity) {
    return entities.contains(entity);
  }

  void addVariableEntity(VariableEntity entity) {
    entities.add(entity);
  }

  private Map<String, Object> getEntityValues(String entity) {
    Map<String, Object> entityValues = table.get(entity);
    if(entityValues == null) {
      table.put(entity, entityValues = Maps.newHashMap());
    }
    return entityValues;
  }

}
