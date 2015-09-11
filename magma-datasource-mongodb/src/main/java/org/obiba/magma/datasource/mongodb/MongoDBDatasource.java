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

import java.util.Date;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.bson.BSONObject;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.magma.type.DateTimeType;

import com.google.common.collect.ImmutableSet;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

public class MongoDBDatasource extends AbstractDatasource {

  public static final String TYPE = "mongodb";

  public static final int MAX_BATCH_SIZE = 1000;

  static final String TIMESTAMPS_FIELD = "_timestamps";

  static final String TIMESTAMPS_CREATED_FIELD = "created";

  static final String TIMESTAMPS_UPDATED_FIELD = "updated";

  private static final String DATASOURCE_COLLECTION = "datasource";

  private static final String VALUE_TABLE_COLLECTION = "value_table";

  @NotNull
  private final MongoDBFactory mongoDBFactory;

  private DBObject dbObject;

  private int batchSize = 100;

  /**
   * See <a href="http://docs.mongodb.org/manual/reference/connection-string">MongoDB connection string specifications</a>.
   *
   * @param name
   * @param mongoURI
   */
  public MongoDBDatasource(@NotNull String name, @NotNull MongoDBFactory mongoDBFactory) {
    super(name, TYPE);
    this.mongoDBFactory = mongoDBFactory;
  }

  @NotNull
  MongoDBFactory getMongoDBFactory() {
    return mongoDBFactory;
  }

  DBCollection getValueTableCollection() {
    return mongoDBFactory.execute(new MongoDBFactory.MongoDBCallback<DBCollection>() {
      @Override
      public DBCollection doWithDB(DB db) {
        DBCollection collection = db.getCollection(VALUE_TABLE_COLLECTION);
        collection.createIndex("datasource");
        collection.createIndex("name");
        return collection;
      }
    });
  }

  DBCollection getDatasourceCollection() {
    return mongoDBFactory.execute(new MongoDBFactory.MongoDBCallback<DBCollection>() {
      @Override
      public DBCollection doWithDB(DB db) {
        DBCollection collection =  db.getCollection(DATASOURCE_COLLECTION);
        collection.createIndex("name");
        return collection;
      }
    });
  }

  DBObject asDBObject() {
    if(dbObject == null) {
      dbObject = getDatasourceCollection().findOne(BasicDBObjectBuilder.start() //
          .add("name", getName()) //
          .get());
      if(dbObject == null) {
        dbObject = BasicDBObjectBuilder.start() //
            .add("name", getName()) //
            .add(TIMESTAMPS_FIELD, createTimestampsObject()).get();
        getDatasourceCollection().insert(dbObject, WriteConcern.ACKNOWLEDGED);
      }
    }
    return dbObject;
  }

  void setLastUpdate(Date date) {
    ((BSONObject) asDBObject().get(TIMESTAMPS_FIELD)).put("updated", date);
    getDatasourceCollection().save(asDBObject());

  }

  static DBObject createTimestampsObject() {
    return BasicDBObjectBuilder.start() //
        .add(TIMESTAMPS_CREATED_FIELD, new Date()) //
        .add(TIMESTAMPS_UPDATED_FIELD, new Date()).get();
  }

  @Override
  protected void onInitialise() {
    mongoDBFactory.getMongoClient();
  }

  @Override
  protected void onDispose() {
    mongoDBFactory.close();
  }

  @Override
  public boolean canDropTable(String tableName) {
    return hasValueTable(tableName);
  }

  @Override
  public void dropTable(@NotNull String tableName) {
    MongoDBValueTable valueTable = (MongoDBValueTable) getValueTable(tableName);
    valueTable.drop();
    removeValueTable(tableName);
  }

  @Override
  public boolean canRenameTable(String tableName) {
    return hasValueTable(tableName);
  }

  @Override
  public void renameTable(String tableName, String newName) {
    if(hasValueTable(newName)) throw new MagmaRuntimeException("A table already exists with the name: " + newName);

    MongoDBValueTable valueTable = (MongoDBValueTable) getValueTable(tableName);

    valueTable.asDBObject().put("name", newName);
    ((BSONObject) valueTable.asDBObject().get(TIMESTAMPS_FIELD)).put("updated", new Date());
    getValueTableCollection().save(valueTable.asDBObject(), WriteConcern.ACKNOWLEDGED);
    removeValueTable(valueTable);

    MongoDBValueTable newTable = new MongoDBValueTable(this, newName);
    Initialisables.initialise(newTable);
    addValueTable(newTable);
  }

  @Override
  @SuppressWarnings("MethodReturnAlwaysConstant")
  public boolean canDrop() {
    return true;
  }

  @Override
  public void drop() {
    for(String name : getValueTableNames()) {
      dropTable(name);
    }
    getDatasourceCollection().remove(BasicDBObjectBuilder.start().add("_id", getName()).get());
  }

  @NotNull
  @Override
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    MongoDBValueTable valueTable = null;
    if(getValueTableNames().isEmpty()) {
      // make sure datasource document exists
      asDBObject();
    }
    if(hasValueTable(tableName)) {
      valueTable = (MongoDBValueTable) getValueTable(tableName);
    } else {
      addValueTable(valueTable = new MongoDBValueTable(this, tableName, entityType));
      setLastUpdate(new Date());
    }

    return new MongoDBValueTableWriter(valueTable);
  }

  @Override
  protected Set<String> getValueTableNames() {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    try(DBCursor cursor = getValueTableCollection()
        .find(BasicDBObjectBuilder.start().add("datasource", asDBObject().get("_id")).get(),
            BasicDBObjectBuilder.start().add("name", 1).get())) {
      while(cursor.hasNext()) {
        builder.add(cursor.next().get("name").toString());
      }
    }
    return builder.build();
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new MongoDBValueTable(this, tableName);
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    ImmutableSet.Builder<Timestamped> builder = ImmutableSet.builder();
    builder.addAll(getValueTables()).add(new MongoDBDatasourceTimestamped());
    return new UnionTimestamps(builder.build());
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    if (batchSize < 1 || batchSize > MAX_BATCH_SIZE)
      throw new IllegalArgumentException(String.format("batchSize should be between 1 and %s", MAX_BATCH_SIZE));

    this.batchSize = batchSize;
  }

  private class MongoDBDatasourceTimestamped implements Timestamped {
    @NotNull
    @Override
    public Timestamps getTimestamps() {

      return new Timestamps() {

        private final BSONObject timestampsObject = (BSONObject) asDBObject().get(TIMESTAMPS_FIELD);

        @NotNull
        @Override
        public Value getLastUpdate() {
          return DateTimeType.get().valueOf(timestampsObject.get(TIMESTAMPS_UPDATED_FIELD));
        }

        @NotNull
        @Override
        public Value getCreated() {
          return DateTimeType.get().valueOf(timestampsObject.get(TIMESTAMPS_CREATED_FIELD));
        }
      };
    }
  }
}
