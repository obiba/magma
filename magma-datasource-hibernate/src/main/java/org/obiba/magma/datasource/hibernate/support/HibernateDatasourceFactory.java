/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.support;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;

@SuppressWarnings("UnusedDeclaration")
public class HibernateDatasourceFactory extends AbstractDatasourceFactory implements Initialisable, Disposable {

  @NotNull
  private SessionFactoryProvider sessionFactoryProvider;

  /**
   * Empty constructor for XML serialization
   */
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
      justification = "Needed by XStream")
  public HibernateDatasourceFactory() { }

  public HibernateDatasourceFactory(String name, @NotNull SessionFactoryProvider sessionFactoryProvider) {
    setName(name);
    this.sessionFactoryProvider = sessionFactoryProvider;
  }

  @NotNull
  @Override
  public Datasource internalCreate() {
    return new HibernateDatasource(getName(), sessionFactoryProvider.getSessionFactory());
  }

  public void setSessionFactoryProvider(@NotNull SessionFactoryProvider sessionFactoryProvider) {
    this.sessionFactoryProvider = sessionFactoryProvider;
  }

  @NotNull
  public SessionFactoryProvider getSessionFactoryProvider() {
    return sessionFactoryProvider;
  }

  @Override
  public void initialise() {
    Initialisables.initialise(sessionFactoryProvider);
  }

  @Override
  public void dispose() {
    Disposables.dispose(sessionFactoryProvider);
  }

}
