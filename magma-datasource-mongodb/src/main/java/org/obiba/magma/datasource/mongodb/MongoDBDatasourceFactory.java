/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb;

import javax.annotation.Nonnull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;

public class MongoDBDatasourceFactory extends AbstractDatasourceFactory {

  private String host = MongoDBDatasource.DEFAULT_HOST;

  private int port = MongoDBDatasource.DEFAULT_PORT;

  public void setHost(String host) {
    this.host = host;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Nonnull
  @Override
  protected Datasource internalCreate() {
    return new MongoDBDatasource(getName(), host, port);
  }
}
