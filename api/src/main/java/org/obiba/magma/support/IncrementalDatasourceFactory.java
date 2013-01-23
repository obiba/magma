/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.support;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;

public class IncrementalDatasourceFactory extends AbstractDatasourceFactory {

  private final DatasourceFactory wrappedFactory;

  private final DatasourceFactory destination;

  public IncrementalDatasourceFactory(DatasourceFactory wrappedFactory, DatasourceFactory destination) {
    this.wrappedFactory = wrappedFactory;
    this.destination = destination;
  }

  @Override
  public void setName(String name) {
    wrappedFactory.setName(name);
  }

  @Override
  public String getName() {
    return wrappedFactory.getName();
  }

  @Override
  protected Datasource internalCreate() {
    return new IncrementalDatasource(wrappedFactory.create(), destination.create());
  }

}