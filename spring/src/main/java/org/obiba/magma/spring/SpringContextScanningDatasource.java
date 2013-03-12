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

import java.util.Set;

import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.Initialisables;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class SpringContextScanningDatasource extends AbstractDatasource {

  @Autowired
  private Set<ValueTableFactoryBeanProvider> valueTableFactoryBeanProviders;

  @Autowired
  private Set<ValueTableFactoryBean> valueTableFactoryBeans;

  public SpringContextScanningDatasource(String name) {
    super(name, "spring-context");
  }

  /*
   * Added to fix ONYX-1436. This method allows reloading a ValueTable. It's implemented here because this instance
   * holds a ValueTableFactory which should be able to re-create the instance when required.
   */
  public void reloadValueTable(String name) throws NoSuchValueTableException {
    if(hasValueTable(name)) {
      removeValueTable(name);
    }
    ValueTable table = initialiseValueTable(name);
    Initialisables.initialise(table);
    addValueTable(table);
  }

  /*
   * Added to fix ONYX-1436. This method allows adding a new ValueTable that was not present in the spring-context when
   * starting.
   */
  public void addValueTable(ValueTableFactoryBean tableFactory) {
    // Make a copy: we don't know if the injected set is mutable.
    Set<ValueTableFactoryBean> newTables = Sets.newLinkedHashSet(this.valueTableFactoryBeans);
    newTables.add(tableFactory);
    this.valueTableFactoryBeans = newTables;
    reloadValueTable(tableFactory.getValueTableName());
  }

  public void dropValueTable(final String name) {
    if(hasValueTable(name)) {
      removeValueTable(name);
    }

    Set<ValueTableFactoryBean> factories = Sets
        .newLinkedHashSet(Iterables.filter(this.valueTableFactoryBeans, new Predicate<ValueTableFactoryBean>() {

          @Override
          public boolean apply(ValueTableFactoryBean input) {
            return input.getValueTableName().equals(name) == false;
          }

        }));
    this.valueTableFactoryBeans = factories;
    // We cannot remove the table from the ValueTableFactoryBeanProvider instances.
  }

  @Override
  protected Set<String> getValueTableNames() {
    Set<String> names = Sets.newHashSet();

    for(ValueTableFactoryBean factory : getAllValueTableFactoryBeans()) {
      names.add(factory.getValueTableName());
    }

    return names;
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    for(ValueTableFactoryBean factory : getAllValueTableFactoryBeans()) {
      if(factory.getValueTableName().equals(tableName)) {
        return factory.buildValueTable(this);
      }
    }

    throw new NoSuchValueTableException(tableName);
  }

  private Iterable<ValueTableFactoryBean> getAllValueTableFactoryBeans() {
    Set<ValueTableFactoryBean> allValueTableFactoryBeans = Sets.newHashSet();

    // Include injected factory beans.
    allValueTableFactoryBeans.addAll(valueTableFactoryBeans);

    // Include factory beans from injected providers.
    for(ValueTableFactoryBeanProvider provider : valueTableFactoryBeanProviders) {
      allValueTableFactoryBeans.addAll(provider.getValueTableFactoryBeans());
    }

    return allValueTableFactoryBeans;
  }
}
