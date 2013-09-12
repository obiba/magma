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
import java.util.Set;

import javax.annotation.Nonnull;

import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;
import org.obiba.magma.type.BinaryType;

import com.google.common.collect.Sets;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

import static org.obiba.magma.datasource.mongodb.MongoDBValueTable.TIMESTAMPS_FIELD;

class MongoDBValueTableWriter implements ValueTableWriter {

  static final String GRID_FILE_ID = "_grid_file_id";

  static final String GRID_FILE_SIZE = "size";

  private final MongoDBValueTable table;

  MongoDBValueTableWriter(@Nonnull MongoDBValueTable table) {
    this.table = table;
  }

  @Override
  public VariableWriter writeVariables() {
    return new MongoDBVariableWriter();
  }

  @Nonnull
  @Override
  public ValueSetWriter writeValueSet(@Nonnull VariableEntity entity) {
    return new MongoDBValueSetWriter(entity);
  }

  @Override
  public void close() throws IOException {
    DBObject tableObject = table.asDBObject();
    BSONObject timestamps = (BSONObject) tableObject.get(TIMESTAMPS_FIELD);
    timestamps.put("updated", new Date());
    // insert or update
    table.getValueTableCollection().save(tableObject);
  }

  private class MongoDBValueSetWriter implements ValueTableWriter.ValueSetWriter {

    private final VariableEntity entity;

    private DBObject valueSetObject;

    private final Set<GridFSInputFile> files = Sets.newHashSet();

    private MongoDBValueSetWriter(VariableEntity entity) {
      this.entity = entity;
    }

    private BSONObject getValueSetObject() {
      if(valueSetObject == null) {
        DBObject template = BasicDBObjectBuilder.start("_id", entity.getIdentifier()).get();
        valueSetObject = table.getValueSetCollection().findOne(template);
        if(valueSetObject == null) {
          valueSetObject = template;
          valueSetObject.put(TIMESTAMPS_FIELD, table.createTimestampsObject());
        }
      }
      return valueSetObject;
    }

    @Override
    public void writeValue(@Nonnull Variable variable, Value value) {
      String field = VariableConverter.normalizeFieldName(variable.getName());
      if(BinaryType.get().equals(value.getValueType())) {
        DBObject fileMetadata = getValueSetObject().containsField(field)
            ? updateBinary(variable, value, field)
            : createBinary(variable, value);
        getValueSetObject().put(field, fileMetadata);
      } else {
        getValueSetObject().put(field, ValueConverter.marshall(variable, value));
      }
    }

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
      BasicDBObjectBuilder metaDataBuilder = BasicDBObjectBuilder.start() //
          .add("datasource", table.getDatasource().getName()) //
          .add("table", table.getName()) //
          .add("variable", variable.getName()) //
          .add("entity", entity.getIdentifier());
      if(occurrence != null) metaDataBuilder.add("occurrence", occurrence);

      GridFSInputFile gridFSFile = table.getGridFS().createFile((byte[]) value.getValue());
      gridFSFile.setMetaData(metaDataBuilder.get());

      files.add(gridFSFile);
      return getBinaryValueMetadata(gridFSFile, occurrence);
    }

    @Override
    public void close() throws IOException {
      BSONObject timestamps = (BSONObject) getValueSetObject().get(TIMESTAMPS_FIELD);
      timestamps.put("updated", new Date());
      table.getValueSetCollection().save(valueSetObject);
      for(GridFSInputFile file : files) {
        file.save();
      }
      files.clear();
    }

    private DBObject getBinaryValueMetadata(GridFSInputFile gridFSFile, Integer occurrence) {
      BasicDBObjectBuilder builder = BasicDBObjectBuilder.start() //
          .add(GRID_FILE_ID, gridFSFile.getId().toString()) //
          .add(GRID_FILE_SIZE, gridFSFile.getLength());
      if(occurrence != null) builder.add("occurrence", occurrence);
      return builder.get();
    }
  }

  private class MongoDBVariableWriter implements ValueTableWriter.VariableWriter {
    @Override
    public void writeVariable(@Nonnull Variable variable) {
      if(table.getVariablesCollection().findOne(BasicDBObjectBuilder.start("_id", variable.getName()).get()) == null) {
        table.addVariableValueSource(new MongoDBVariableValueSource(table, variable.getName()));
      }
      // insert or update
      DBObject varObject = VariableConverter.marshall(variable);
      table.getVariablesCollection().save(varObject);
    }

    @Override
    public void close() throws IOException {

    }
  }
}
