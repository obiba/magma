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

import javax.annotation.Nonnull;

import org.bson.BSONObject;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.magma.type.DateTimeType;

import com.google.common.collect.ImmutableSet;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBPort;
import com.mongodb.WriteConcern;

public class MongoDBDatasource extends AbstractDatasource {

  public static final String TYPE = "mongodb";

  static final String DEFAULT_HOST = "localhost";

  static final int DEFAULT_PORT = DBPort.PORT;

  static final String TIMESTAMPS_FIELD = "_timestamps";

  static final String TIMESTAMPS_CREATED_FIELD = "created";

  static final String TIMESTAMPS_UPDATED_FIELD = "updated";

  private static final String DATASOURCE_COLLECTION = "datasource";

  private static final String VALUE_TABLE_COLLECTION = "value_table";

  @Nonnull
  private final MongoDBFactory mongoDBFactory;

  /**
   * See <a href="http://docs.mongodb.org/manual/reference/connection-string">MongoDB connection string specifications</a>.
   *
   * @param name
   * @param mongoURI
   */
  public MongoDBDatasource(@Nonnull String name, @Nonnull MongoDBFactory mongoDBFactory) {
    super(name, TYPE);
    this.mongoDBFactory = mongoDBFactory;
  }

  DB getDB() {
    return mongoDBFactory.getDB();
  }

  @Nonnull
  MongoDBFactory getMongoDBFactory() {
    return mongoDBFactory;
  }

  DBCollection getValueTableCollection() {
    return getDB().getCollection(VALUE_TABLE_COLLECTION);
  }

  DBCollection getDatasourceCollection() {
    return getDB().getCollection(DATASOURCE_COLLECTION);
  }

  DBObject asDBObject() {
    DBObject dsObject = getDatasourceCollection().findOne(BasicDBObjectBuilder.start() //
        .add("_id", getName()) //
        .get());

    if(dsObject == null) {
      dsObject = BasicDBObjectBuilder.start() //
          .add("_id", getName()) //
          .add(TIMESTAMPS_FIELD, createTimestampsObject()).get();
      getDatasourceCollection().insert(dsObject, WriteConcern.ACKNOWLEDGED);
    }

    return dsObject;
  }

  void setLastUpdate(Date date) {
    DBObject dsObject = asDBObject();
    BSONObject timestamps = (BSONObject) dsObject.get(TIMESTAMPS_FIELD);
    timestamps.put("updated", date);
    getDatasourceCollection().save(dsObject);
  }

  static DBObject createTimestampsObject() {
    return BasicDBObjectBuilder.start().add(TIMESTAMPS_CREATED_FIELD, new Date())
        .add(TIMESTAMPS_UPDATED_FIELD, new Date()).get();
  }

  @Override
  protected void onInitialise() {
    mongoDBFactory.getMongoClient();
  }

  @Override
  public boolean canDropTable(String tableName) {
    return hasValueTable(tableName);
  }

  @Override
  public void dropTable(@Nonnull String tableName) {
    MongoDBValueTable valueTable = (MongoDBValueTable) getValueTable(tableName);
    valueTable.drop();
    removeValueTable(tableName);
  }

  @Override
  public boolean canDrop() {
    return true;
  }

  @Override
  public void drop() {
    for(String valueTable : getValueTableNames()) {
      dropTable(valueTable);
    }
    getDatasourceCollection().remove(BasicDBObjectBuilder.start().add("_id", getName()).get());
  }

  @Nonnull
  @Override
  public ValueTableWriter createWriter(@Nonnull String tableName, @Nonnull String entityType) {
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
    DBCursor cursor = getValueTableCollection().find(BasicDBObjectBuilder.start().add("datasource", getName()).get(),
        BasicDBObjectBuilder.start().add("name", 1).get());
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    try {
      while(cursor.hasNext()) {
        builder.add(cursor.next().get("name").toString());
      }
    } finally {
      cursor.close();
    }
    return builder.build();
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new MongoDBValueTable(this, tableName);
  }

  @Nonnull
  @Override
  public Timestamps getTimestamps() {
    ImmutableSet.Builder<Timestamped> builder = ImmutableSet.builder();
    builder.addAll(getValueTables()).add(new MongoDBDatasourceTimestamped());
    return new UnionTimestamps(builder.build());
  }

  private class MongoDBDatasourceTimestamped implements Timestamped {
    @Nonnull
    @Override
    public Timestamps getTimestamps() {
      return new Timestamps() {

        private BSONObject getTimestampsObject() {
          return (BSONObject) asDBObject().get(TIMESTAMPS_FIELD);
        }

        @Nonnull
        @Override
        public Value getLastUpdate() {
          return DateTimeType.get().valueOf(getTimestampsObject().get(TIMESTAMPS_UPDATED_FIELD));
        }

        @Nonnull
        @Override
        public Value getCreated() {
          return DateTimeType.get().valueOf(getTimestampsObject().get(TIMESTAMPS_CREATED_FIELD));
        }
      };
    }
  }
}
