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

import java.io.IOException;
import java.util.Date;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;
import org.obiba.magma.type.BinaryType;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

class MongoDBValueTableWriter implements ValueTableWriter {

  static final String GRID_FILE_ID = "_grid_file_id";

  static final String GRID_FILE_SIZE = "size";

  static final String GRID_FILE_MD5 = "md5";

  private final MongoDBValueTable table;

  MongoDBValueTableWriter(@NotNull MongoDBValueTable table) {
    this.table = table;
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
  public void close() throws IOException {
    updateLastUpdate();
  }

  private void updateLastUpdate() {
    table.setLastUpdate(new Date());
  }

  private class MongoDBValueSetWriter implements ValueTableWriter.ValueSetWriter {

    private final VariableEntity entity;

    private DBObject valueSetObject;

    private MongoDBValueSetWriter(VariableEntity entity) {
      this.entity = entity;
    }

    private DBObject getValueSetObject() {
      if(valueSetObject == null) {
        DBObject template = BasicDBObjectBuilder.start("_id", entity.getIdentifier()).get();
        valueSetObject = table.getValueSetCollection().findOne(template);
        if(valueSetObject == null) {
          valueSetObject = template;
          valueSetObject.put(MongoDBDatasource.TIMESTAMPS_FIELD, MongoDBDatasource.createTimestampsObject());
        }
      }
      return valueSetObject;
    }

    @Override
    public void writeValue(@NotNull Variable variable, Value value) {
      String field = table.findVariable(variable.getName()).get("_id").toString();
      if(BinaryType.get().equals(value.getValueType())) {
        DBObject fileMetadata = getValueSetObject().containsField(field)
            ? updateBinary(variable, value, field)
            : createBinary(variable, value);
        getValueSetObject().put(field, fileMetadata);
      } else {
        getValueSetObject().put(field, ValueConverter.marshall(variable, value));
      }
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
        GridFS gridFS = table.getGridFS();
        if(variable.isRepeatable()) {
          for(BSONObject obj : (Iterable<BSONObject>) binaryValueMetaData) {
            gridFS.remove((ObjectId) obj.get("_id"));
          }
        } else {
          gridFS.remove((ObjectId) binaryValueMetaData.get("_id"));
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

      GridFSInputFile gridFSFile = table.getGridFS().createFile((byte[]) value.getValue());
      gridFSFile.setMetaData(metaDataBuilder.get());
      gridFSFile.save();
      return getBinaryValueMetadata(gridFSFile, occurrence);
    }

    @Override
    public void close() throws IOException {
      updateValueSetLastUpdate();
    }

    private void updateValueSetLastUpdate() {
      BSONObject timestamps = (BSONObject) getValueSetObject().get(MongoDBDatasource.TIMESTAMPS_FIELD);
      timestamps.put(MongoDBDatasource.TIMESTAMPS_UPDATED_FIELD, new Date());
      table.getValueSetCollection().save(getValueSetObject());
      updateLastUpdate();
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
      if(table.findVariable(variable.getName()) == null) {
        table.addVariableValueSource(new MongoDBVariableValueSource(table, variable.getName()));
      }
      // insert or update
      DBObject varObject = VariableConverter.marshall(variable);
      table.getVariablesCollection().save(varObject);

      updateLastUpdate();
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

      updateLastUpdate();
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
        table.getGridFS().remove(new ObjectId((String) fileMetadata.get(GRID_FILE_ID)));
      }
    }

    @Override
    public void close() throws IOException {
    }
  }

}
