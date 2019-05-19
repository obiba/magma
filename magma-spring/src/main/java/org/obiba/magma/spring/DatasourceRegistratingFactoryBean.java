/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.spring;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.springframework.beans.factory.InitializingBean;

public class DatasourceRegistratingFactoryBean implements InitializingBean {

  private Set<Datasource> datasources;

  public void setDatasources(Set<Datasource> datasources) {
    this.datasources = datasources;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    MagmaEngine engine = MagmaEngine.get();
    for(Datasource ds : datasources) {
      engine.addDatasource(ds);
    }
  }

}
