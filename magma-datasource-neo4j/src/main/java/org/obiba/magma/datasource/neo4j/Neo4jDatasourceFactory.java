/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j;

import javax.annotation.Nonnull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

public class Neo4jDatasourceFactory extends AbstractDatasourceFactory implements Initialisable, Disposable {

  @Nonnull
  private AutowireCapableBeanFactory autowireCapableBeanFactory;

  /**
   * Empty constructor for XML serialization
   */
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
      justification = "Needed by XStream")
  public Neo4jDatasourceFactory() { }

  public Neo4jDatasourceFactory(String name, @Nonnull AutowireCapableBeanFactory autowireCapableBeanFactory) {
    this.autowireCapableBeanFactory = autowireCapableBeanFactory;
    setName(name);
  }

  @Nonnull
  @Override
  public Datasource internalCreate() {
    Neo4jDatasource neo4jDatasource = new Neo4jDatasource(getName());

    // seems easier than @Configurable approach
    autowireCapableBeanFactory.autowireBean(neo4jDatasource);

    return neo4jDatasource;
  }

  @Override
  public void initialise() {
    Initialisables.initialise(autowireCapableBeanFactory);
  }

  @Override
  public void dispose() {
    Disposables.dispose(autowireCapableBeanFactory);
  }

}
