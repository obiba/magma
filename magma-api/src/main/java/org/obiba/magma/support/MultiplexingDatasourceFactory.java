/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.support;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.support.DatasourceCopier.VariableTransformer;
import org.obiba.magma.support.MultiplexingDatasource.ValueTableMultiplexer;

public class MultiplexingDatasourceFactory extends AbstractDatasourceFactory {

  private final ValueTableMultiplexer tableMultiplexer;

  private final VariableTransformer variableTransformer;

  private final DatasourceFactory wrappedFactory;

  public MultiplexingDatasourceFactory(DatasourceFactory wrappedFactory, ValueTableMultiplexer tableMultiplexer,
      VariableTransformer variableTransformer) {
    this.tableMultiplexer = tableMultiplexer;
    this.variableTransformer = variableTransformer;
    this.wrappedFactory = wrappedFactory;
  }

  @Override
  public void setName(@NotNull String name) {
    wrappedFactory.setName(name);
  }

  @Override
  public String getName() {
    return wrappedFactory.getName();
  }

  @NotNull
  @Override
  protected Datasource internalCreate() {
    return new MultiplexingDatasource(wrappedFactory.create(), tableMultiplexer, variableTransformer);
  }

}