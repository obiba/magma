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

import java.net.UnknownHostException;

import javax.annotation.Nonnull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;

import com.mongodb.ServerAddress;

public class MongoDBDatasourceFactory extends AbstractDatasourceFactory {

  private String database;

  private String host = MongoDBDatasource.DEFAULT_HOST;

  private int port = MongoDBDatasource.DEFAULT_PORT;

  /**
   * Mongo database name.
   * @param database
   */
  public void setDatabase(String database) {
    this.database = database;
  }

  /**
   * Mongo server host.
   * @param host
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Mongo server port number.
   * @param port
   */
  public void setPort(int port) {
    this.port = port;
  }

  @Nonnull
  @Override
  protected Datasource internalCreate() {
    try {
      return new MongoDBDatasource(getName(), database == null ? getName() : database, new ServerAddress(host, port));
    } catch(UnknownHostException e) {
      throw new MagmaRuntimeException(e);
    }
  }
}
