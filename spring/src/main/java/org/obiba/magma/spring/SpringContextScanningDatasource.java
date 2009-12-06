/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.spring;

import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceMetaData;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 *
 */
public class SpringContextScanningDatasource implements Datasource, ApplicationContextAware {

  private ApplicationContext applicationContext;

  private String name;

  private DatasourceMetaData metadata;

  private Set<ValueTable> valueTables;

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setMetadata(DatasourceMetaData metadata) {
    this.metadata = metadata;
  }

  @Override
  public DatasourceMetaData getMetaData() {
    return metadata;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return "spring-context";
  }

  @Override
  public ValueTableWriter createWriter(String tableName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ValueTable getValueTable(final String name) {
    try {
      return Iterables.find(valueTables, new Predicate<ValueTable>() {
        @Override
        public boolean apply(ValueTable input) {
          return input.getName().equals(name);
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchValueTableException(name);
    }
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return ImmutableSet.copyOf(valueTables);
  }

  @Override
  public void initialise() {
    valueTables = Sets.newHashSet();
    for(ValueTableFactoryBean factory : (Iterable<ValueTableFactoryBean>) applicationContext.getBeansOfType(ValueTableFactoryBean.class).values()) {
      valueTables.add(factory.buildValueTable(this));
    }

    for(Initialisable init : Iterables.filter(valueTables, Initialisable.class)) {
      init.initialise();
    }
  }

  @Override
  public void dispose() {
    for(Disposable disposable : Iterables.filter(getValueTables(), Disposable.class)) {
      disposable.dispose();
    }
  }

}
