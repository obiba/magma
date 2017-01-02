/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.beans;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.support.VariableEntityProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class BeanValueTable extends AbstractValueTable {

  private final Set<ValueSetBeanResolver> resolvers = Sets.newHashSet();

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

      @NotNull
      @Override
      public Timestamps getTimestamps() {
        return BeanValueTable.this.getTimestamps();
      }

      @Override
      public ValueTable getValueTable() {
        return BeanValueTable.this;
      }

      @Override
      public Object resolve(final Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
        try {
          return Iterables.find(getResolvers(), new Predicate<ValueSetBeanResolver>() {
            @Override
            public boolean apply(ValueSetBeanResolver input) {
              return input.resolves(type);
            }
          }).resolve(type, valueSet, variable);
        } catch(NoSuchElementException e) {
          throw new NoSuchBeanException(valueSet, type,
              "No resolver for bean of type " + type + " in table " + getName());
        }
      }
    };
  }

  protected Iterable<ValueSetBeanResolver> getResolvers() {
    return resolvers;
  }

  public void addResolver(ValueSetBeanResolver resolver) {
    resolvers.add(resolver);
  }

  // TODO: these were overridden to increase visibility. We should find an alternative to doing this.
  @Override
  public void addVariableValueSource(VariableValueSource source) {
    super.addVariableValueSource(source);
  }

  @Override
  public void addVariableValueSources(Collection<VariableValueSource> sources) {
    super.addVariableValueSources(sources);
  }

  @Override
  public void addVariableValueSources(VariableValueSourceFactory factory) {
    super.addVariableValueSources(factory);
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return NullTimestamps.get();
  }

}
