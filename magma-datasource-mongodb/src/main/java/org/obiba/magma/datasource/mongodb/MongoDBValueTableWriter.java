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
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;
import org.obiba.magma.type.BinaryType;

import com.google.common.collect.Sets;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

import static org.obiba.magma.datasource.mongodb.MongoDBValueTable.TIMESTAMPS_FIELD;

class MongoDBValueTableWriter implements ValueTableWriter {

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
        if(getValueSetObject().containsField(field)) {
          updateBinary(variable, value);
        } else {
          createBinary(variable, value);
        }
      } else {
        getValueSetObject().put(field, ValueConverter.marshall(variable, value));
      }
    }

    private void updateBinary(Variable variable, Value value) {
      if(value.isNull()) {
        // TODO remove file
      }
    }

    private void createBinary(Variable variable, Value value) {
      GridFS gridFS = table.getGridFS();
      if(value.isNull()) {
      } else {
        if(variable.isRepeatable()) {
          for(Value val : value.asSequence().getValues()) {
            GridFSInputFile gridFSFile = gridFS.createFile((byte[]) val.getValue());
            files.add(gridFSFile);
          }
        } else {
          GridFSInputFile gridFSFile = gridFS.createFile((byte[]) value.getValue());
          files.add(gridFSFile);
        }
      }
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
