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
import org.obiba.magma.ValueTable;

public class IncrementalDatasourceFactory extends AbstractDatasourceFactory {

  private final ValueTable destinationTable;

  private final DatasourceFactory wrappedFactory;

  public IncrementalDatasourceFactory(DatasourceFactory wrappedFactory, ValueTable destinationTable) {
    this.destinationTable = destinationTable;
    this.wrappedFactory = wrappedFactory;
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
    return new IncrementalDatasource(wrappedFactory.create(), destinationTable);
  }

}