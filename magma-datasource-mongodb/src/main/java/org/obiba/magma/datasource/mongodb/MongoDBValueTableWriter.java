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
import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.obiba.magma.*;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.type.BinaryType;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class MongoDBValueTableWriter implements ValueTableWriter {

  static final String GRID_FILE_ID = "_grid_file_id";

  static final String GRID_FILE_SIZE = "size";

  static final String GRID_FILE_MD5 = "md5";

  private final MongoDBValueTable table;

  private boolean hasValueSets;

  private final Set<String> identifiersAtInit;

  private final List<DBObject> batch = Lists.newArrayList();

  MongoDBValueTableWriter(@NotNull MongoDBValueTable table) {
    this.table = table;
    this.hasValueSets = table.getValueSetCount()>0;
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
    List<DBObject> toSave = null;

    synchronized(table) {
      if(!batch.isEmpty()) {
        toSave = ImmutableList.copyOf(batch);
        batch.clear();
      }
    }

    if(toSave != null) insertOrReplaceBatch(toSave);

    updateLastUpdate();
  }

  private void updateLastUpdate() {
    table.setLastUpdate(new Date());
  }

  private void insertOrReplaceBatch(List<DBObject> toSave) {
    BulkWriteOperation bulkWriteOperation = table.getValueSetCollection().initializeOrderedBulkOperation();

    for (DBObject obj: toSave)
    {
      bulkWriteOperation.find(BasicDBObjectBuilder.start("_id", obj.get("_id")).get())//
          .upsert()//
          .replaceOne(obj);
    }

    bulkWriteOperation.execute();
  }

  private class MongoDBValueSetWriter implements ValueTableWriter.ValueSetWriter {

    private final VariableEntity entity;

    private DBObject valueSetObject;

    private boolean removed = false;

    private MongoDBValueSetWriter(VariableEntity entity) {
      this.entity = entity;
    }

    private DBObject getValueSetObject() {
      if(valueSetObject == null) {
        if (identifiersAtInit.contains(entity.getIdentifier())) {
          DBObject template = BasicDBObjectBuilder.start("_id", entity.getIdentifier()).get();
          valueSetObject = table.getValueSetCollection().findOne(template);
        }
        if(valueSetObject == null) {
          valueSetObject = BasicDBObjectBuilder.start("_id", entity.getIdentifier()).get();
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
      if(BinaryType.get().equals(value.getValueType())) {
        DBObject fileMetadata = getValueSetObject().containsField(field)
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
      for(Variable variable : table.getVariables()) {
        if(BinaryType.get().equals(variable.getValueType())) {
          String field = ((MongoDBVariable) variable).getId();
          removeFile(variable, field);
        }
      }
      // then remove value set document
      table.getValueSetCollection().remove(BasicDBObjectBuilder.start("_id", entity.getIdentifier()).get());
    }

    @Nullable
    private DBObject updateBinary(Variable variable, Value value, String field) {
      removeFile(variable, field);
      return value.isNull() ? null : createBinary(variable, value);
    }

    @SuppressWarnings("unchecked")
    private void removeFile(Variable variable, String field) {
      BSONObject binaryValueMetaData = (BSONObject) getValueSetObject().get(field);
      if(binaryValueMetaData != null) {
        GridFS gridFS = table.getMongoDBFactory().getGridFS();
        if(variable.isRepeatable()) {
          for(BSONObject obj : (Iterable<BSONObject>) binaryValueMetaData) {
            gridFS.remove(new ObjectId((String) obj.get(GRID_FILE_ID)));
          }
        } else {
          gridFS.remove(new ObjectId((String) binaryValueMetaData.get(GRID_FILE_ID)));
        }
      }
    }

    @Nullable
    private DBObject createBinary(Variable variable, Value value) {
      if(value.isNull()) {
        return null;
      }
      if(variable.isRepeatable()) {
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
      if(value.isNull()) {
        return getBinaryValueMetadata(null, occurrence);
      }
      BasicDBObjectBuilder metaDataBuilder = BasicDBObjectBuilder.start() //
          .add("datasource", table.getDatasource().getName()) //
          .add("table", table.getName()) //
          .add("variable", variable.getName()) //
          .add("entity", entity.getIdentifier());
      if(occurrence != null) metaDataBuilder.add("occurrence", occurrence);

      GridFSInputFile gridFSFile = table.getMongoDBFactory().getGridFS().createFile((byte[]) value.getValue());
      gridFSFile.setMetaData(metaDataBuilder.get());
      gridFSFile.save();
      return getBinaryValueMetadata(gridFSFile, occurrence);
    }

    @Override
    public void close() {
      if(!removed) {
        int batchSize = ((MongoDBDatasource)table.getDatasource()).getBatchSize();

        if(batchSize == 1) {
          table.getValueSetCollection().save(getValueSetObject());
        } else {
          List<DBObject> toSave = null;
          DBObject valueSet = getValueSetObject();

          synchronized(table) {
            batch.add(valueSet);

            if(batch.size() >= batchSize) {
              toSave = ImmutableList.copyOf(batch);
              batch.clear();
            }
          }

          if(toSave != null) {
            insertOrReplaceBatch(toSave);
          }
        }
      }
    }

    private DBObject getBinaryValueMetadata(@Nullable GridFSInputFile gridFSFile, Integer occurrence) {
      BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
      if(gridFSFile != null) {
        builder.add(GRID_FILE_ID, gridFSFile.getId().toString()) //
            .add(GRID_FILE_SIZE, gridFSFile.getLength()) //
            .add(GRID_FILE_MD5, gridFSFile.getMD5());
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
      DBObject existingDbObject = table.findVariable(variable.getName());
      if(existingDbObject == null) {
        table.addVariableValueSource(new MongoDBVariableValueSource(table, variable.getName()));
      }
      else {
        // reset the cached variable object
        ((MongoDBVariableValueSource)table.getVariableValueSource(variable.getName())).invalidate();
      }
      // insert or update
      DBObject varObject = VariableConverter.marshall(variable);
      if(existingDbObject != null) {
        if (hasValueSets) {
          // some properties cannot be modified if there are already data stored, this affects the data schema of the collection
          Variable existingVariable = VariableConverter.unmarshall(existingDbObject);
          if (!existingVariable.getValueType().equals(variable.getValueType())) {
            throw new IncompatibleValueTypeException(variable.getName(), existingVariable.getValueType(), variable.getValueType());
          }
          if (existingVariable.isRepeatable() != variable.isRepeatable()) {
            throw new IncompatibleRepeatabilityException(variable.getName(), existingVariable.isRepeatable(), variable.isRepeatable());
          }
        }
        varObject.put("_id", existingDbObject.get("_id"));
      }
      table.getVariablesCollection().save(varObject);
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      DBObject varObj = table.findVariable(variable.getName());
      if(varObj == null) return;

      // remove from the variable collection
      table.removeVariableValueSource(variable.getName());
      table.getVariablesCollection().remove(varObj);

      // remove associated values from the value set collection
      removeVariableValues((MongoDBVariable) variable);

      if(table.getVariableCount() == 0) {
        table.getValueSetCollection().remove(BasicDBObjectBuilder.start().get());
      }
    }

    private void removeVariableValues(@NotNull MongoDBVariable variable) {
      DBCollection valueSetCollection = table.getValueSetCollection();
      DBCursor cursor = valueSetCollection.find();
      String field = variable.getId();
      while(cursor.hasNext()) {
        DBObject valueSetObject = cursor.next();
        if(variable.getValueType().equals(BinaryType.get())) {
          removeBinaryFiles(variable, field, valueSetObject);
        }
        valueSetObject.removeField(field);

        BSONObject timestamps = (BSONObject) valueSetObject.get(MongoDBDatasource.TIMESTAMPS_FIELD);
        timestamps.put(MongoDBDatasource.TIMESTAMPS_UPDATED_FIELD, new Date());
        valueSetCollection.save(valueSetObject);
      }
    }

    @SuppressWarnings("unchecked")
    private void removeBinaryFiles(Variable variable, String field, BSONObject valueSetObject) {
      BSONObject fileMetadata = (BSONObject) valueSetObject.get(field);
      if(fileMetadata == null) return;
      if(variable.isRepeatable()) {
        for(BSONObject occurrenceObj : (Iterable<BSONObject>) fileMetadata) {
          removeFile(occurrenceObj);
        }
      } else {
        removeFile(fileMetadata);
      }
    }

    private void removeFile(BSONObject fileMetadata) {
      if(fileMetadata.containsField(GRID_FILE_ID)) {
        table.getMongoDBFactory().getGridFS().remove(new ObjectId((String) fileMetadata.get(GRID_FILE_ID)));
      }
    }

    @Override
    public void close() {
    }
  }

}
