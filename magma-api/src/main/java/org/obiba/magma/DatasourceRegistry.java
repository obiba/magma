/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.magma.support.ValueTableReference;

public interface DatasourceRegistry {

  ValueTableReference createReference(String reference);

  Set<Datasource> getDatasources();

  Datasource getDatasource(String name) throws NoSuchDatasourceException;

  boolean hasDatasource(String name);

  void addDecorator(Decorator<Datasource> decorator);

  Datasource addDatasource(Datasource datasource);

  Datasource addDatasource(DatasourceFactory factory);

  void removeDatasource(Datasource datasource);

  String addTransientDatasource(DatasourceFactory factory);

  boolean hasTransientDatasource(String uid);

  void removeTransientDatasource(@Nullable String uid);

  Datasource getTransientDatasourceInstance(String uid);

}
