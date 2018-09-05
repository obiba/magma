/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb;

import java.io.Serializable;
import java.net.URI;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.mongodb.*;
import org.obiba.magma.MagmaRuntimeException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mongodb.gridfs.GridFS;
import org.obiba.magma.SocketFactoryProvider;

public class MongoDBFactory implements Serializable {

  private static final long serialVersionUID = 2264861208658330136L;

  @NotNull
  private final String connectionURI;

  @Nullable
  private transient MongoClientURI mongoClientURI;

  @Nullable
  private transient MongoClient mongoClient;

  @Nullable
  private SocketFactoryProvider socketFactoryProvider;

  public MongoDBFactory(@NotNull URI uri) {
    this(uri.toString());
  }

  public MongoDBFactory(@NotNull String connectionURI) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(connectionURI), "connectionURI cannot be null or empty");
    this.connectionURI = connectionURI;
  }

  /**
   * Use the provided socket factory to connect to a MongoDB server.
   *
   * @param socketFactoryProvider
   */
  public void setSocketFactoryProvider(@Nullable SocketFactoryProvider socketFactoryProvider) {
    this.socketFactoryProvider = socketFactoryProvider;
  }

  @NotNull
  public String getConnectionURI() {
    return connectionURI;
  }

  @NotNull
  public MongoClientURI getMongoClientURI() {
    if(mongoClientURI == null) {
      mongoClientURI = new MongoClientURI(connectionURI) {

        @Override
        public MongoClientOptions getOptions() {
          MongoClientOptions options = super.getOptions();
          if (socketFactoryProvider == null || !options.isSslEnabled()) return options;
          MongoClientOptions.Builder newOptions = MongoClientOptions.builder(options);
          newOptions.socketFactory(socketFactoryProvider.getSocketFactory());
          return newOptions.build();
        }
      };
    }
    return mongoClientURI;
  }

  @NotNull
  public MongoClient getMongoClient() {
    if(mongoClient == null) {
      try {
        mongoClient = new MongoClient(getMongoClientURI());
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
  public GridFS getGridFS() {
    return execute(new MongoDBCallback<GridFS>() {
      @Override
      public GridFS doWithDB(DB db) {
        return new GridFS(db);
      }
    });
  }

  public <T> T execute(MongoDBCallback<T> callback) {
    return callback.doWithDB(getMongoClient().getDB(getMongoClientURI().getDatabase()));
  }

  public interface MongoDBCallback<T> {
    T doWithDB(DB db);
  }

}
