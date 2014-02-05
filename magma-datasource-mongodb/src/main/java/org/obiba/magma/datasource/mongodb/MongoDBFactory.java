package org.obiba.magma.datasource.mongodb;

import java.io.Serializable;
import java.net.URI;
import java.net.UnknownHostException;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaRuntimeException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.gridfs.GridFS;

public class MongoDBFactory implements Serializable {

  private static final long serialVersionUID = 2264861208658330136L;

  @NotNull
  private final String connectionURI;

  @Nullable
  private transient MongoClientURI mongoClientURI;

  @Nullable
  private transient MongoClient mongoClient;

  public MongoDBFactory(@NotNull URI uri) {
    this(uri.toString());
  }

  public MongoDBFactory(@NotNull String connectionURI) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(connectionURI), "connectionURI cannot be null or empty");
    this.connectionURI = connectionURI;
  }

  @NotNull
  public String getConnectionURI() {
    return connectionURI;
  }

  @NotNull
  public MongoClientURI getMongoClientURI() {
    if(mongoClientURI == null) {
      mongoClientURI = new MongoClientURI(connectionURI);
    }
    return mongoClientURI;
  }

  @NotNull
  public MongoClient getMongoClient() {
    if(mongoClient == null) {
      try {
        mongoClient = new MongoClient(getMongoClientURI());
      } catch(UnknownHostException e) {
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
