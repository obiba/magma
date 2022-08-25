/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.SocketFactoryProvider;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.URI;

public class MongoDBFactory implements Serializable {

  private static final long serialVersionUID = 2264861208658330136L;

  @NotNull
  private final String connectionURI;

  @Nullable
  private transient MongoClient mongoClient;

  private final String databaseName;

  public MongoDBFactory(@NotNull URI uri) {
    this(uri.toString());
  }

  public MongoDBFactory(@NotNull String connectionURI) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(connectionURI), "connectionURI cannot be null or empty");
    this.connectionURI = connectionURI;
    this.databaseName = new ConnectionString(getConnectionURI()).getDatabase();
  }

  /**
   * Use the provided socket factory to connect to a MongoDB server.
   *
   * @param socketFactoryProvider
   */
  @Deprecated
  public void setSocketFactoryProvider(@Nullable SocketFactoryProvider socketFactoryProvider) {
  }

  @NotNull
  public String getConnectionURI() {
    return connectionURI;
  }

  @NotNull
  public MongoClient getMongoClient() {
    if(mongoClient == null) {
      try {
        mongoClient = MongoClients.create(getConnectionURI());
      } catch(MongoException e) {
        throw new MagmaRuntimeException(e);
      }
    }
    return mongoClient;
  }

  public void close() {
    if(mongoClient != null) {
      mongoClient.close();
      mongoClient = null;
    }
  }

  @NotNull
  public GridFSBucket getGridFSBucket() {
    return GridFSBuckets.create(getMongoClient().getDatabase(databaseName));
  }

  public <T> T execute(MongoDBCallback<T> callback) {
    return callback.doWithDB(getMongoClient().getDatabase(databaseName));
  }

  public interface MongoDBCallback<T> {
    T doWithDB(MongoDatabase db);
  }

}
