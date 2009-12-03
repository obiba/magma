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

import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.ValueTable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 *
 */
public class SpringContextScanningDatasource implements Datasource, ApplicationContextAware {

  private ApplicationContext applicationContext;

  private String name;

  private Set<ValueTable> valueTables;

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setName(String name) {
    this.name = name;
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
  public ValueTable getValueTable(String name) {
    return null;
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

}
