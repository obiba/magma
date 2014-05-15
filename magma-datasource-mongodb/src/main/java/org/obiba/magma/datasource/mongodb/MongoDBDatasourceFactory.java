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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.apache.http.client.utils.URIBuilder;
import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

/**
 * Create a MongoDB datasource by either providing a connection string
 * (see <a href="http://docs.mongodb.org/manual/reference/connection-string">MongoDB connection string specifications</a>)
 * or by providing the elements necessary to build the connection string.
 */
public class MongoDBDatasourceFactory extends AbstractDatasourceFactory {

  /**
   * mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
   */
  @NotNull
  private String url;

  private String username;

  private String password;

  private String options;

  public MongoDBDatasourceFactory(@NotNull String name, @NotNull String url) {
    this(name, url, null, null, null);
  }

  public MongoDBDatasourceFactory(@NotNull String name, @NotNull String url, String username, String password,
      String options) {
    setName(name);
    this.url = url;
    this.username = username;
    this.password = password;
    this.options = options;
  }

  @NotNull
  @Override
  protected Datasource internalCreate() {
    return new MongoDBDatasource(getName(), getMongoDBFactory());
  }

  public MongoDBFactory getMongoDBFactory() {
    return new MongoDBFactory(getUri());
  }

  public URI getUri() {
    try {
      URIBuilder uriBuilder = new URIBuilder(url);
      if(!Strings.isNullOrEmpty(username)) {
        if(Strings.isNullOrEmpty(password)) {
          uriBuilder.setUserInfo(username);
        } else {
          uriBuilder.setUserInfo(username, password);
        }
      }
      Properties prop = readOptions();
      for(Map.Entry<Object, Object> entry : prop.entrySet()) {
        uriBuilder.addParameter(entry.getKey().toString(), entry.getValue().toString());
      }
      return uriBuilder.build();
    } catch(URISyntaxException e) {
      throw new RuntimeException("Cannot create MongoDB URI", e);
    }
  }

  public String getMongoDbDatabaseName() {
    try {
      URIBuilder uriBuilder = new URIBuilder(url);
      return uriBuilder.getPath().substring(1);
    } catch(URISyntaxException e) {
      throw new RuntimeException("Cannot parse MongoDB URI", e);
    }
  }

  public String getOptions() {
    return options;
  }

  public void setOptions(String options) {
    this.options = options;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @NotNull
  public String getUrl() {
    return url;
  }

  public void setUrl(@NotNull String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Properties readOptions() {
    Properties prop = new Properties();
    try {
      if(!Strings.isNullOrEmpty(options)) {
        prop.load(new ByteArrayInputStream(options.getBytes(Charsets.UTF_8)));
      }
    } catch(IOException e) {
      // can't really happen
    }
    return prop;
  }

}
