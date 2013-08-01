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
import org.obiba.magma.MagmaRuntimeException;

import com.google.common.base.Strings;
import com.mongodb.MongoClientURI;

/**
 * Create a MongoDB datasource by either providing a connection string
 * (see <a href="http://docs.mongodb.org/manual/reference/connection-string">MongoDB connection string specifications</a>)
 * or by providing the elements necessary to build the connection string.
 */
public class MongoDBDatasourceFactory extends AbstractDatasourceFactory {

  public static final String URI_PREFIX = "mongodb://";

  private String connectionURI;

  private String database;

  private String host;

  private Integer port;

  private String username;

  private String password;

  private String options;

  /**
   * See <a href="http://docs.mongodb.org/manual/reference/connection-string">MongoDB connection string specifications</a>.
   *
   * @param connectionURI
   */
  public void setConnectionURI(String connectionURI) {
    if(Strings.isNullOrEmpty(connectionURI) || !connectionURI.startsWith(URI_PREFIX)) {
      throw new MagmaRuntimeException("Not a valid MongoDB connection URI: " + connectionURI);
    }
    this.connectionURI = connectionURI;
  }

  /**
   * Mongo database name.
   *
   * @param database
   */
  public void setDatabase(String database) {
    this.database = database;
  }

  /**
   * Mongo server host.
   *
   * @param host
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Mongo server port number.
   *
   * @param port
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * MongoDB username.
   *
   * @param username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * MongoDB username password.
   *
   * @param password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * MongoDB options.
   *
   * @param options
   */
  public void setOptions(String options) {
    this.options = options;
  }

  @Nonnull
  @Override
  protected Datasource internalCreate() {
    if(connectionURI == null) {
      StringBuilder uri = new StringBuilder(URI_PREFIX);
      if(username != null) {
        uri.append(username).append(":").append(password).append("@");
      }
      uri.append(host == null ? MongoDBDatasource.DEFAULT_HOST : host);
      uri.append(":").append(port == null ? MongoDBDatasource.DEFAULT_PORT : port);
      uri.append("/").append(database == null ? getName() : database);
      if(options != null) {
        uri.append("?").append(options);
      }
      connectionURI = uri.toString();
    }
    return new MongoDBDatasource(getName(), new MongoClientURI(connectionURI));
  }
}
