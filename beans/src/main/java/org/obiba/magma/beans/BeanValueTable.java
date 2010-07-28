package org.obiba.magma.beans;

import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.DateTimeType;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class BeanValueTable extends AbstractValueTable {

  private Set<ValueSetBeanResolver> resolvers = Sets.newHashSet();

  public BeanValueTable(Datasource datasource, String name, VariableEntityProvider provider) {
    super(datasource, name, provider);
  }

  @Override
  public ValueSet getValueSet(final VariableEntity entity) throws NoSuchValueSetException {
    return new BeanValueSet() {

      @Override
      public VariableEntity getVariableEntity() {
        return entity;
      }

      @Override
      public ValueTable getValueTable() {
        return BeanValueTable.this;
      }

      @Override
      public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
        return BeanValueTable.this.resolve(type, valueSet, variable);
      }
    };
  }

  protected Set<ValueSetBeanResolver> getResolvers() {
    return resolvers;
  }

  public void addResolver(ValueSetBeanResolver resolver) {
    resolvers.add(resolver);
  }

  private Object resolve(final Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    try {
      return Iterables.find(getResolvers(), new Predicate<ValueSetBeanResolver>() {
        @Override
        public boolean apply(ValueSetBeanResolver input) {
          return input.resolves(type);
        }
      }).resolve(type, valueSet, variable);
    } catch(NoSuchElementException e) {
      throw new NoSuchBeanException(valueSet, type, "No resolver for bean of type " + type + " in table " + getName());
    }
  }

  @Override
  public Timestamps getTimestamps(ValueSet valueSet) {
    return new Timestamps() {

      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().nullValue();
      }

      @Override
      public Value getCreated() {
        return DateTimeType.get().nullValue();
      }
    };
  }

}
