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
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

/**
 *
 */
public class SpringContextScanningDatasource extends AbstractDatasource {

  @Autowired
  private Set<ValueTableFactoryBean> valueTableFactories;

  public SpringContextScanningDatasource(String name) {
    super(name, "spring-context");
  }

  @Override
  protected Set<String> getValueTableNames() {
    Set<String> names = Sets.newHashSet();
    for(ValueTableFactoryBean factory : valueTableFactories) {
      names.add(factory.getValueTableName());
    }
    return names;
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    for(ValueTableFactoryBean factory : valueTableFactories) {
      if(factory.getValueTableName().equals(tableName)) {
        return factory.buildValueTable(this);
      }
    }
    throw new NoSuchValueTableException(tableName);
  }

}
