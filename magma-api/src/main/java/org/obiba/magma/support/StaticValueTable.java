/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
public class StaticValueTable extends AbstractValueTable {

  private final String entityType;

  private final List<VariableEntity> entities;

  private final Map<String, Map<String, Object>> table = Maps.newHashMap();

  public StaticValueTable(Datasource datasource, String name, Iterable<String> entities, String entityType) {
    super(datasource, name);

    this.name = name;

    this.entityType = entityType == null ? "" : entityType;

    this.entities = Lists.newArrayList(asVariableEntities(entities));

    setVariableEntityProvider(new VariableEntityProvider() {

      @Override
      public boolean isForEntityType(String type) {
        return getEntityType().equals(type);
      }

      @NotNull
      @Override
      public List<VariableEntity> getVariableEntities() {
        return StaticValueTable.this.entities;
      }

      @NotNull
      @Override
      public String getEntityType() {
        return StaticValueTable.this.entityType;
      }
    });
  }

  public StaticValueTable(Datasource datasource, String name, Iterable<String> entities) {
    this(datasource, name, entities, "Participant");
  }

  private Iterable<VariableEntity> asVariableEntities(Iterable<String> entities) {
    List<VariableEntity> variableEntities = Lists.newArrayList();
    if (entities == null) return variableEntities;
    for (String entity : entities) {
      variableEntities.add(new VariableEntityBean(StaticValueTable.this.entityType, entity));
    }
    return variableEntities;
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
    addVariableValueSource(new AbstractVariableValueSource() {

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

      @Override
      public boolean supportVectorSource() {
        return false;
      }

      @NotNull
      @Override
      @SuppressWarnings("unchecked")
      public VectorSource asVectorSource() {
        throw new VectorSourceNotSupportedException((Class<? extends ValueSource>) getClass());
      }

      @NotNull
      @Override
      public Variable getVariable() {
        return variable;
      }
    });
  }

  public void addVariables(final ValueType type, String... variables) {
    for (final String variable : variables) {
      addVariableValueSource(new AbstractVariableValueSource() {

        @NotNull
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

        @Override
        public boolean supportVectorSource() {
          return false;
        }

        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public VectorSource asVectorSource() {
          throw new VectorSourceNotSupportedException((Class<? extends ValueSource>) getClass());
        }
      });
    }
  }

  public StaticValueTable addValues(String entity, Object... variableAndValues) {
    for (int i = 0; i < variableAndValues.length; i += 2) {
      Object variable = variableAndValues[i];
      if (variable instanceof Variable) {
        Variable var = (Variable) variable;
        if (!hasVariable(var.getName())) {
          addVariable(var);
        }
        variable = var.getName();
      }
      Object value = variableAndValues[i + 1];
      getEntityValues(entity).put(variable.toString(), value);
    }
    return this;
  }

  public StaticValueTable removeValues(String entity) {
    if (table.containsKey(entity)) {
      table.remove(entity);
      entities.remove(new VariableEntityBean(StaticValueTable.this.entityType, entity));
    }
    return this;
  }

  public StaticValueTable removeAllValues() {
    entities.clear();
    table.clear();
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
    if (entityValues == null) {
      table.put(entity, entityValues = Maps.newHashMap());
    }
    return entityValues;
  }

}
