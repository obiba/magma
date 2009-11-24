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

import org.obiba.magma.Collection;
import org.obiba.magma.ValueSetProvider;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.CollectionBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.ImmutableSet;

/**
 *
 */
public class SpringContextScanningDatasource extends AbstractDatasource implements ApplicationContextAware {

  private ApplicationContext applicationContext;

  private String collectionName;

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setCollectionName(String collectionName) {
    this.collectionName = collectionName;
  }

  public String getCollectionName() {
    return collectionName;
  }

  @Override
  protected Set<String> getCollectionNames() {
    return ImmutableSet.of(collectionName);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Collection initialiseCollection(String collection) {
    CollectionBuilder builder = new CollectionBuilder(getCollectionName());
    for(ValueSetProvider provider : (Iterable<ValueSetProvider>) applicationContext.getBeansOfType(ValueSetProvider.class).values()) {
      builder.add(provider);
    }
    for(VariableValueSourceFactory factory : (Iterable<VariableValueSourceFactory>) applicationContext.getBeansOfType(VariableValueSourceFactory.class).values()) {
      builder.add(factory.createSources(getCollectionName()));
    }
    return builder.build(this);
  }
}
