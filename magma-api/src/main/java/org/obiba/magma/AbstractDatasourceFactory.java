/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import javax.validation.constraints.NotNull;

public abstract class AbstractDatasourceFactory implements DatasourceFactory {
  //
  // Instance Variables
  //

  @Deprecated
  private DatasourceTransformer transformer;

  private String name;

  @NotNull
  protected abstract Datasource internalCreate();

  //
  // DatasourceFactory Methods
  //

  @SuppressWarnings("ConstantConditions")
  @Override
  public void setName(@NotNull String name) {
    if(name == null) throw new IllegalArgumentException("Datasource name cannot be null.");
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Datasource create() {
    return internalCreate();
  }

  @Override
  public void setDatasourceTransformer(DatasourceTransformer transformer) {
    this.transformer = transformer;
  }

  @Override
  public DatasourceTransformer getDatasourceTransformer() {
    return transformer;
  }

}
