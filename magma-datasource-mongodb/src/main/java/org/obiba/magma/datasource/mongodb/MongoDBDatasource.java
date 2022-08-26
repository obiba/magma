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

import com.google.common.collect.ImmutableSet;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.obiba.magma.*;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.magma.type.DateTimeType;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

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

  private Document document;

  private int batchSize = 100;

  /**
   * See <a href="http://docs.mongodb.org/manual/reference/connection-string">MongoDB connection string specifications</a>.
   *
   * @param name
   * @param mongoDBFactory
   */
  public MongoDBDatasource(@NotNull String name, @NotNull MongoDBFactory mongoDBFactory) {
    super(name, TYPE);
    this.mongoDBFactory = mongoDBFactory;
  }

  @NotNull
  MongoDBFactory getMongoDBFactory() {
    return mongoDBFactory;
  }

  MongoCollection<Document> getValueTableCollection() {
    return mongoDBFactory.execute(new MongoDBFactory.MongoDBCallback<MongoCollection<Document>>() {
      @Override
      public MongoCollection<Document> doWithDB(MongoDatabase db) {
        MongoCollection<Document> collection = db.getCollection(VALUE_TABLE_COLLECTION);
        collection.createIndex(Indexes.ascending("datasource", "name"));
        return collection;
      }
    });
  }

  MongoCollection<Document> getDatasourceCollection() {
    return mongoDBFactory.execute(new MongoDBFactory.MongoDBCallback<MongoCollection<Document>>() {
      @Override
      public MongoCollection<Document> doWithDB(MongoDatabase db) {
        MongoCollection<Document> collection =  db.getCollection(DATASOURCE_COLLECTION);
        collection.createIndex(Indexes.text("name"));
        return collection;
      }
    });
  }

  Document asDocument() {
    if(document == null) {
      document = getDatasourceCollection().find(Filters.eq("name", getName())).first();
      if(document == null) {
        document = new Document()
            .append("name", getName())
            .append(TIMESTAMPS_FIELD, createTimestampsObject());
        getDatasourceCollection().insertOne(document);
      }
    }
    return document;
  }

  void setLastUpdate(Date date) {
    ((Document) asDocument().get(TIMESTAMPS_FIELD)).put("updated", date);
    getDatasourceCollection().replaceOne(Filters.eq("name", getName()), asDocument());

  }

  static Document createTimestampsObject() {
    return new Document()
        .append(TIMESTAMPS_CREATED_FIELD, new Date())
        .append(TIMESTAMPS_UPDATED_FIELD, new Date());
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

    Document vtObject = valueTable.asDocument();
    vtObject.put("name", newName);
    ((Document) vtObject.get(TIMESTAMPS_FIELD)).put("updated", new Date());
    getValueTableCollection().replaceOne(Filters.eq("_id", vtObject.get("_id")), vtObject);
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
    getDatasourceCollection().deleteOne(Filters.eq("name", getName()));
  }

  @NotNull
  @Override
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    MongoDBValueTable valueTable = null;
    if(getValueTableNames().isEmpty()) {
      // make sure datasource document exists
      asDocument();
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
    try(MongoCursor<Document> cursor = getValueTableCollection()
        .find(Filters.eq("datasource", asDocument().get("_id")))
            .projection(Projections.include("name")).cursor()) {
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

        private final Document timestampsObject = (Document) asDocument().get(TIMESTAMPS_FIELD);

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
