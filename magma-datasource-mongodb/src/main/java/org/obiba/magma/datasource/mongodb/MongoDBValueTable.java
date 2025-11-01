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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.obiba.magma.*;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.type.DateTimeType;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MongoDBValueTable extends AbstractValueTable {

  private static final String VARIABLE_SUFFIX = "_variable";

  private static final String VALUE_SET_SUFFIX = "_value_set";

  private Document document;

  public MongoDBValueTable(@NotNull Datasource datasource, @NotNull String name) {
    this(datasource, name, null);
  }

  public MongoDBValueTable(@NotNull Datasource datasource, @NotNull String name, @Nullable String entityType) {
    super(datasource, name);
    setVariableEntityProvider(new MongoDBVariableEntityProvider(this, entityType));
    // ensure corresponding document is stored
    asDocument();
  }

  MongoDBFactory getMongoDBFactory() {
    return getMongoDBDatasource().getMongoDBFactory();
  }

  MongoCollection<Document> getValueTableCollection() {
    return getMongoDBDatasource().getValueTableCollection();
  }

  MongoCollection<Document> getVariablesCollection() {
    return getMongoDBFactory().execute(db -> {
      MongoCollection<Document> collection = db.getCollection(getId() + VARIABLE_SUFFIX);
      collection.createIndex(Indexes.text("name"));
      return collection;
    });
  }

  MongoCollection<Document> getValueSetCollection() {
    return getMongoDBFactory().execute(db -> db.getCollection(getId() + VALUE_SET_SUFFIX));
  }

  Document asDocument() {
    if (document == null) {
      document = getValueTableCollection().find(Filters.and(
          Filters.eq("datasource", getMongoDBDatasource().asDocument().get("_id")),
          Filters.eq("name", getName()))).first();
      // create DBObject if not found
      if (document == null) {
        document = new Document()
            .append("datasource", getMongoDBDatasource().asDocument().get("_id"))
            .append("name", getName())
            .append("entityType", getEntityType())
            .append(MongoDBDatasource.TIMESTAMPS_FIELD, MongoDBDatasource.createTimestampsObject());
        getValueTableCollection().insertOne(document);
      }
    }
    return document;
  }

  void setLastUpdate(Date date) {
    ((Document) asDocument().get(MongoDBDatasource.TIMESTAMPS_FIELD)).put("updated", date);
    getValueTableCollection().replaceOne(Filters.eq("_id", asDocument().get("_id")), asDocument());
    getMongoDBDatasource().setLastUpdate(date);
  }

  private String getId() {
    return asDocument().get("_id").toString();
  }

  @Override
  protected void addVariableValueSource(VariableValueSource source) {
    super.addVariableValueSource(source);
  }

  @Override
  protected void removeVariableValueSource(String variableName) {
    super.removeVariableValueSource(variableName);
  }

  @Override
  public void initialise() {
    addVariableValueSources(new MongoDBVariableValueSourceFactory(this));
    super.initialise();
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    if (!hasValueSet(entity)) {
      throw new NoSuchValueSetException(this, entity);
    }
    return super.getValueSetTimestamps(entity);
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(final Iterable<VariableEntity> entities) {
    if (!entities.iterator().hasNext()) {
      return ImmutableList.of();
    }
    return () -> new TimestampsIterator(entities.iterator());
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    return getMongoDBVariableEntityProvider().hasVariableEntity(entity);
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    if (!hasValueSet(entity)) {
      throw new NoSuchValueSetException(this, entity);
    }
    return new MongoDBValueSet(this, entity);
  }

  @Override
  protected ValueSetBatch getValueSetsBatch(List<VariableEntity> entities) {
    return new MongoDBValueSetBatch(this, entities);
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {

      private final Document timestampsObject = (Document) asDocument().get(MongoDBDatasource.TIMESTAMPS_FIELD);

      @NotNull
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(timestampsObject.get(MongoDBDatasource.TIMESTAMPS_UPDATED_FIELD));
      }

      @NotNull
      @Override
      public Value getCreated() {
        return DateTimeType.get().valueOf(timestampsObject.get(MongoDBDatasource.TIMESTAMPS_CREATED_FIELD));
      }
    };
  }

  public void drop() {
    // drop associated collections
    dropFiles();
    getValueSetCollection().drop();
    getVariablesCollection().drop();
    getValueTableCollection().deleteOne(Filters.eq("_id", asDocument().get("_id")));
    getMongoDBDatasource().setLastUpdate(new Date());
    document = null;
  }

  Document findVariable(String variableName) {
    return getVariablesCollection().find(Filters.eq("name", variableName)).first();
  }

  @Override
  public int getVariableCount() {
    return (int) getVariablesCollection().countDocuments(new Document(), new CountOptions().hintString("_id_"));
  }

  @Override
  public int getValueSetCount() {
    return getMongoDBVariableEntityProvider().getVariableEntityCount();
  }

  @Override
  public int getVariableEntityCount() {
    return getMongoDBVariableEntityProvider().getVariableEntityCount();
  }

  @Override
  public List<VariableEntity> getVariableEntities(int offset, int limit) {
    return getMongoDBVariableEntityProvider().getVariableEntities(offset, limit);
  }

  @Override
  public boolean canDropValueSets() {
    return true;
  }

  @Override
  public void dropValueSets() {
    dropFiles();
    getValueSetCollection().drop();
    setLastUpdate(new Date());
  }

  private MongoDBDatasource getMongoDBDatasource() {
    return ((MongoDBDatasource) getDatasource());
  }

  private MongoDBVariableEntityProvider getMongoDBVariableEntityProvider() {
    return (MongoDBVariableEntityProvider) getVariableEntityProvider();
  }

  /**
   * Drop the files from the {@link com.mongodb.client.gridfs.GridFSBucket} for this table.
   */
  private void dropFiles() {
    Document tableDoc = asDocument();
    GridFSBucket gridFSBucket = getMongoDBDatasource().getMongoDBFactory().getGridFSBucket();
    try (MongoCursor<GridFSFile> cursor = gridFSBucket
        .find(Filters.and(
            Filters.exists("metadata.version"),
            Filters.eq("metadata.datasource_id", tableDoc.get("datasource")),
            Filters.eq("metadata.table_id", tableDoc.get("_id"))))
        .cursor()) {
      while (cursor.hasNext()) {
        GridFSFile file = cursor.next();
        gridFSBucket.delete(file.getObjectId());
      }
    }
    // legacy
    try (MongoCursor<GridFSFile> cursor = gridFSBucket
        .find(Filters.and(
            Filters.not(Filters.exists("metadata.version")),
            Filters.eq("metadata.datasource", getDatasource().getName()),
            Filters.eq("metadata.table", getName())))
        .cursor()) {
      while (cursor.hasNext()) {
        GridFSFile file = cursor.next();
        gridFSBucket.delete(file.getObjectId());
      }
    }
  }

  private class TimestampsIterator implements Iterator<Timestamps> {

    private final Iterator<VariableEntity> entities;

    private final MongoCursor<Document> cursor;

    private final Map<String, Timestamps> timestampsMap = Maps.newHashMap();

    private TimestampsIterator(Iterator<VariableEntity> entities) {
      this.entities = entities;
      cursor = getValueSetCollection()
          .find()
          .projection(Projections.include(MongoDBDatasource.TIMESTAMPS_FIELD))
          .cursor();
    }

    @Override
    public boolean hasNext() {
      return entities.hasNext();
    }

    @Override
    public Timestamps next() {
      VariableEntity entity = entities.next();

      if (timestampsMap.containsKey(entity.getIdentifier())) return getTimestampsFromMap(entity);

      boolean found = false;
      while (cursor.hasNext() && !found) {
        Document obj = cursor.next();
        String id = obj.get("_id").toString();
        Document timestamps = (Document) obj.get(MongoDBDatasource.TIMESTAMPS_FIELD);
        timestampsMap.put(id,
            new TimestampsBean(ValueConverter.unmarshall(DateTimeType.get(), timestamps.get("created")),
                ValueConverter.unmarshall(DateTimeType.get(), timestamps.get("updated")))
        );
        found = id.equals(entity.getIdentifier());
      }

      if (timestampsMap.containsKey(entity.getIdentifier())) return getTimestampsFromMap(entity);
      return NullTimestamps.get();
    }

    /**
     * No duplicate of entities, so remove timestamps from map once get.
     *
     * @param entity
     * @return
     */
    private Timestamps getTimestampsFromMap(VariableEntity entity) {
      Timestamps value = timestampsMap.get(entity.getIdentifier());
      timestampsMap.remove(entity.getIdentifier());
      return value;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
