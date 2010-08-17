package org.obiba.magma.support;

import java.util.Map;
import java.util.Set;

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

public class StaticValueTable extends AbstractValueTable {

  private final Set<VariableEntity> entities;

  private final Map<String, Map<String, Object>> table = Maps.newHashMap();

  public StaticValueTable(Datasource datasource, String name, Set<String> entities) {
    super(datasource, name);
    this.entities = ImmutableSet.copyOf(Iterables.transform(entities, new Function<String, VariableEntity>() {

      @Override
      public VariableEntity apply(String from) {
        return new VariableEntityBean("", from);
      }
    }));
    super.setVariableEntityProvider(new VariableEntityProvider() {

      @Override
      public boolean isForEntityType(String entityType) {
        return true;
      }

      @Override
      public Set<VariableEntity> getVariableEntities() {
        return StaticValueTable.this.entities;
      }

      @Override
      public String getEntityType() {
        return "";
      }
    });
  }

  public void addVariables(final ValueType type, String... variables) {
    for(final String variable : variables) {
      super.addVariableValueSource(new VariableValueSource() {

        @Override
        public Variable getVariable() {
          return Variable.Builder.newVariable(variable, type, "").build();
        }

        @Override
        public Value getValue(ValueSet valueSet) {
          return type.valueOf(table.get(valueSet.getVariableEntity().getIdentifier()).get(variable));
        }

        @Override
        public ValueType getValueType() {
          return type;
        }

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
      Object value = variableAndValues[i + 1];
      getEntityValues(entity).put(variable.toString(), value);
    }
    return this;
  }

  @Override
  public Timestamps getTimestamps(ValueSet valueSet) {
    return NullTimestamps.get();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new ValueSetBean(this, entity);
  }

  private Map<String, Object> getEntityValues(String entity) {
    Map<String, Object> entityValues = table.get(entity);
    if(entityValues == null) {
      table.put(entity, entityValues = Maps.newHashMap());
    }
    return entityValues;
  }

}
