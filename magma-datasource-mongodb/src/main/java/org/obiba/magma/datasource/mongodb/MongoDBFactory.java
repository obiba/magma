package org.obiba.magma.datasource.mongodb;

import java.io.Serializable;
import java.net.UnknownHostException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.MagmaRuntimeException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.gridfs.GridFS;

public class MongoDBFactory implements Serializable {

  private static final long serialVersionUID = 2264861208658330136L;

  @Nonnull
  private final String connectionURI;

  @Nullable
  private transient MongoClientURI mongoClientURI;

  @Nullable
  private transient MongoClient mongoClient;

  @Nullable
  private transient DB db;

  @Nullable
  private transient GridFS gridFS;

  public MongoDBFactory(@Nonnull String connectionURI) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(connectionURI), "connectionURI cannot be null or empty");
    this.connectionURI = connectionURI;
  }

  @Nonnull
  public String getConnectionURI() {
    return connectionURI;
  }

  @Nonnull
  public MongoClientURI getMongoClientURI() {
    if(mongoClientURI == null) {
      mongoClientURI = new MongoClientURI(connectionURI);
    }
    return mongoClientURI;
  }

  @Nonnull
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

  @Nonnull
  public DB getDB() {
    if(db == null) {
      db = getMongoClient().getDB(getMongoClientURI().getDatabase());
    }
    return db;
  }

  @Nonnull
  public GridFS getGridFS() {
    if(gridFS == null) {
      gridFS = new GridFS(getDB());
    }
    return gridFS;
  }
}
