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

import javax.annotation.Nonnull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.Initialisable;

public class BatchDatasourceFactory extends AbstractDatasourceFactory implements Initialisable {

  @Nonnull
  private final DatasourceFactory wrappedFactory;

  private final int limit;

  public BatchDatasourceFactory(@Nonnull DatasourceFactory wrappedFactory, int limit) {
    this.wrappedFactory = wrappedFactory;
    this.limit = limit;
  }

  @Override
  public void setName(String name) {
    wrappedFactory.setName(name);
  }

  @Override
  public String getName() {
    return wrappedFactory.getName();
  }

  @Nonnull
  @Override
  protected Datasource internalCreate() {
    return new BatchDatasource(wrappedFactory.create(), limit);
  }

  @Override
  public void initialise() {
    Initialisables.initialise(wrappedFactory);
  }
}