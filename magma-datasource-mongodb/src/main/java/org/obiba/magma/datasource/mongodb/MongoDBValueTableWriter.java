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
import com.google.common.collect.Lists;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.obiba.magma.*;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;
import org.obiba.magma.support.VariableHelper;
import org.obiba.magma.type.BinaryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class MongoDBValueTableWriter implements ValueTableWriter {

  private static final Logger log = LoggerFactory.getLogger(MongoDBValueTableWriter.class);

  static final String GRID_FILE_ID = "_grid_file_id";

  static final String GRID_FILE_SIZE = "size";

  static final String GRID_FILE_MD5 = "md5";

  private final MongoDBValueTable table;

  private boolean hasValueSets;

  private final Set<String> identifiersAtInit;

  private final List<WriteModel<Document>> batch = Lists.newArrayList();

  MongoDBValueTableWriter(@NotNull MongoDBValueTable table) {
    this.table = table;
    this.hasValueSets = table.getValueSetCount() > 0;
    // might be a costly call but (1) most likely this will be empty and (2) will spare hasValueSet() calls
    this.identifiersAtInit = table.getVariableEntities().stream()
        .map(VariableEntity::getIdentifier).collect(Collectors.toSet());
  }

  @Override
  public VariableWriter writeVariables() {
    return new MongoDBVariableWriter();
  }

  @NotNull
  @Override
  public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
    return new MongoDBValueSetWriter(entity);
  }

  @Override
  public void close() {
    List<WriteModel<Document>> toSave = null;

    synchronized (table) {
      if (!batch.isEmpty()) {
        toSave = ImmutableList.copyOf(batch);
        batch.clear();
      }
    }

    if (toSave != null) insertOrReplaceBatch(toSave);

    updateLastUpdate();
  }

  private void updateLastUpdate() {
    table.setLastUpdate(new Date());
  }

  private void insertOrReplaceBatch(List<WriteModel<Document>> toSave) {
    try {
      table.getValueSetCollection().bulkWrite(toSave);
    } catch (MongoBulkWriteException e) {
      log.error("A MongoBulkWriteException occurred with the following message: {}", e.getMessage(), e);
      throw new MagmaRuntimeException(e);
    }
  }

  private class MongoDBValueSetWriter implements ValueTableWriter.ValueSetWriter {

    private final VariableEntity entity;

    private Document valueSetObject;

    private boolean newValueSet = false;

    private boolean removed = false;

    private MongoDBValueSetWriter(VariableEntity entity) {
      this.entity = entity;
    }

    private Document getValueSetObject() {
      if (valueSetObject == null) {
        if (identifiersAtInit.contains(entity.getIdentifier())) {
          valueSetObject = table.getValueSetCollection().find(Filters.eq("_id", entity.getIdentifier())).first();
        }
        if (valueSetObject == null) {
          newValueSet = true;
          valueSetObject = new Document("_id", entity.getIdentifier());
          valueSetObject.put(MongoDBDatasource.TIMESTAMPS_FIELD, MongoDBDatasource.createTimestampsObject());
        }
      }
      return valueSetObject;
    }

    @Override
    public void writeValue(@NotNull Variable variable, Value value) {
      hasValueSets = true;
      removed = false;
      MongoDBVariable varObj = (MongoDBVariable) table.getVariable(variable.getName());
      String field = varObj.getId();
      if (BinaryType.get().equals(value.getValueType())) {
        DBObject fileMetadata = getValueSetObject().containsKey(field)
            ? updateBinary(variable, value, field)
            : createBinary(variable, value);
        getValueSetObject().put(field, fileMetadata);
      } else {
        getValueSetObject().put(field, ValueConverter.marshall(variable, value));
      }
    }

    @Override
    public void remove() {
      removed = true;
      // remove files if any
      for (Variable variable : table.getVariables()) {
        if (BinaryType.get().equals(variable.getValueType())) {
          String field = ((MongoDBVariable) variable).getId();
          removeFile(variable, field);
        }
      }
      // then remove value set document
      table.getValueSetCollection().deleteOne(Filters.eq("_id", entity.getIdentifier()));
    }

    @Nullable
    private DBObject updateBinary(Variable variable, Value value, String field) {
      removeFile(variable, field);
      return value.isNull() ? null : createBinary(variable, value);
    }

    @SuppressWarnings("unchecked")
    private void removeFile(Variable variable, String field) {
      Document binaryValueMetaData = (Document) getValueSetObject().get(field);
      if (binaryValueMetaData != null) {
        GridFSBucket gridFS = table.getMongoDBFactory().getGridFSBucket();
        if (variable.isRepeatable()) {
          for (Document obj : (Iterable<Document>) binaryValueMetaData) {
            gridFS.delete(new ObjectId(obj.get(GRID_FILE_ID).toString()));
          }
        } else {
          gridFS.delete(new ObjectId(binaryValueMetaData.get(GRID_FILE_ID).toString()));
        }
      }
    }

    @Nullable
    private DBObject createBinary(Variable variable, Value value) {
      if (value.isNull()) {
        return null;
      }
      if (variable.isRepeatable()) {
        int occurrence = 0;
        BasicDBList metadata = new BasicDBList();
        for(Value occurrenceValue : value.asSequence().getValues()) {
          metadata.add(createFile(variable, occurrenceValue, occurrence++));
        }
        return metadata;
      }
      return createFile(variable, value, null);
    }

    private DBObject createFile(Variable variable, Value value, Integer occurrence) {
      if (value.isNull()) {
        return getBinaryValueMetadata(null, occurrence);
      }
      Document tableDoc = table.asDocument();
      Document metadataDoc = new Document()
          .append("version", 2)
          .append("datasource_id", tableDoc.getObjectId("datasource"))
          .append("table_id", tableDoc.getObjectId("_id"))
          .append("variable", variable.getName())
          .append("entity", entity.getIdentifier());
      if (occurrence != null) metadataDoc.append("occurrence", occurrence);

      GridFSUploadOptions options = new GridFSUploadOptions()
          .chunkSizeBytes(1048576) // 1MB
          .metadata(metadataDoc);

      GridFSFile gridFSFile = null;
      try (InputStream in = new ByteArrayInputStream((byte[]) value.getValue())) {
        ObjectId fileId = table.getMongoDBFactory().getGridFSBucket().uploadFromStream("", in, options);
        gridFSFile = table.getMongoDBFactory().getGridFSBucket().find(Filters.eq("_id", fileId)).first();
      } catch (IOException e) {
        log.error("GridFS write error: {}", e.getMessage(), e);
        throw new MagmaRuntimeException(e);
      }

      return getBinaryValueMetadata(gridFSFile, occurrence);
    }

    @Override
    public void close() {
      if (!removed) {
        int batchSize = ((MongoDBDatasource) table.getDatasource()).getBatchSize();

        Document valueSet = getValueSetObject();
        if (batchSize == 1) {
          if (newValueSet)
            table.getValueSetCollection().insertOne(valueSet);
          else
            table.getValueSetCollection().replaceOne(Filters.eq("_id", valueSet.get("_id")), valueSet);
        } else {
          List<WriteModel<Document>> toSave = null;

          synchronized (table) {
            batch.add(newValueSet ? new InsertOneModel<>(valueSet) : new ReplaceOneModel<>(Filters.eq("_id", valueSet.get("_id")), valueSet));

            if (batch.size() >= batchSize) {
              toSave = ImmutableList.copyOf(batch);
              batch.clear();
            }
          }

          if (toSave != null) {
            insertOrReplaceBatch(toSave);
          }
        }
      }
    }

    private DBObject getBinaryValueMetadata(@Nullable GridFSFile gridFSFile, Integer occurrence) {
      BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
      if(gridFSFile != null) {
        builder.add(GRID_FILE_ID, gridFSFile.getId().asObjectId())
            .add(GRID_FILE_SIZE, gridFSFile.getLength());
      }
      if(occurrence != null) builder.add("occurrence", occurrence);
      return builder.get();
    }
  }

  private class MongoDBVariableWriter implements ValueTableWriter.VariableWriter {

    @Override
    public void writeVariable(@NotNull Variable variable) {
      if (!table.getEntityType().equals(variable.getEntityType())) {
        throw new IncompatibleEntityTypeException(variable.getName(), table.getEntityType(), variable.getEntityType());
      }
      Document existingDbObject = table.findVariable(variable.getName());
      if (existingDbObject == null) {
        table.addVariableValueSource(new MongoDBVariableValueSource(table, variable.getName()));
      } else {
        // reset the cached variable object
        ((MongoDBVariableValueSource) table.getVariableValueSource(variable.getName())).invalidate();
      }
      // insert or update
      Document varObject = VariableConverter.marshall(variable);
      if (existingDbObject != null) {
        Variable existingVariable = VariableConverter.unmarshall(existingDbObject);
        // check if variable has changed
        boolean modified = VariableHelper.isModified(existingVariable, variable);
        if (modified) {
          if (hasValueSets) {
            // some properties cannot be modified if there are already data stored, this affects the data schema of the collection
            if (!existingVariable.getValueType().equals(variable.getValueType())) {
              throw new IncompatibleValueTypeException(variable.getName(), existingVariable.getValueType(), variable.getValueType());
            }
            if (existingVariable.isRepeatable() != variable.isRepeatable()) {
              throw new IncompatibleRepeatabilityException(variable.getName(), existingVariable.isRepeatable(), variable.isRepeatable());
            }
          }
          varObject.put("_id", existingDbObject.get("_id"));
        } else {
          varObject = null;
        }
      }
      if (varObject != null) {
        if (existingDbObject != null) {
          UpdateResult result = table.getVariablesCollection().replaceOne(Filters.eq("_id", varObject.get("_id")), varObject);
          log.debug("Variable update result: {}", result);
        }
        else {
          InsertOneResult result = table.getVariablesCollection().insertOne(varObject);
          log.debug("Variable insert result: {}", result);
        }
      }
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      Document varObj = table.findVariable(variable.getName());
      if (varObj == null) return;

      // remove from the variable collection
      table.removeVariableValueSource(variable.getName());
      table.getVariablesCollection().deleteOne(Filters.eq("_id", varObj.get("_id")));

      // remove associated values from the value set collection
      removeVariableValues((MongoDBVariable) variable);

      // truncate table if no variables
      if (table.getVariableCount() == 0) {
        table.getValueSetCollection().deleteMany(Filters.empty());
      }
    }

    private void removeVariableValues(@NotNull MongoDBVariable variable) {
      MongoCollection<Document> valueSetCollection = table.getValueSetCollection();
      try (MongoCursor<Document> cursor = valueSetCollection.find().cursor()) {
        String field = variable.getId();
        while (cursor.hasNext()) {
          Document valueSetObject = cursor.next();
          if (variable.getValueType().equals(BinaryType.get())) {
            removeBinaryFiles(variable, field, valueSetObject);
          }
          valueSetObject.remove(field);

          Document timestamps = (Document) valueSetObject.get(MongoDBDatasource.TIMESTAMPS_FIELD);
          timestamps.put(MongoDBDatasource.TIMESTAMPS_UPDATED_FIELD, new Date());
          valueSetCollection.replaceOne(Filters.eq("_id", valueSetObject.get("_id")), valueSetObject);
        }
      }
    }

    @SuppressWarnings("unchecked")
    private void removeBinaryFiles(Variable variable, String field, Document valueSetObject) {
      Document fileMetadata = (Document) valueSetObject.get(field);
      if (fileMetadata == null) return;
      if (variable.isRepeatable()) {
        for (Document occurrenceObj : (Iterable<Document>) fileMetadata) {
          removeFile(occurrenceObj);
        }
      } else {
        removeFile(fileMetadata);
      }
    }

    private void removeFile(Document fileMetadata) {
      if (fileMetadata.containsKey(GRID_FILE_ID)) {
        table.getMongoDBFactory().getGridFSBucket().delete(new ObjectId(fileMetadata.get(GRID_FILE_ID).toString()));
      }
    }

    @Override
    public void close() {
    }
  }

}
