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

import java.util.List;

import javax.annotation.Nonnull;

import org.bson.BSONObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueLoaderFactory;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import static org.obiba.magma.datasource.mongodb.MongoDBValueTableWriter.GRID_FILE_ID;
import static org.obiba.magma.datasource.mongodb.MongoDBValueTableWriter.GRID_FILE_MD5;
import static org.obiba.magma.datasource.mongodb.MongoDBValueTableWriter.GRID_FILE_SIZE;

class MongoDBValueSet implements ValueSet {

  private final MongoDBValueTable valueTable;

  private final VariableEntity entity;

  private BSONObject object;

  MongoDBValueSet(MongoDBValueTable valueTable, VariableEntity entity) {
    this.valueTable = valueTable;
    this.entity = entity;
  }

  @Override
  public ValueTable getValueTable() {
    return valueTable;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return entity;
  }

  Value getValue(Variable variable) {
    BSONObject valueObject = getDBObject();
    ValueType valueType = variable.getValueType();
    return valueType.equals(BinaryType.get())
        ? getBinaryValue(valueObject, variable)
        : ValueConverter.unmarshall(variable, valueObject);
  }

  @SuppressWarnings("unchecked")
  private Value getBinaryValue(BSONObject valueObject, Variable variable) {
    ValueLoaderFactory factory = new MongoDBValueLoaderFactory(valueTable.getMongoDBFactory());
    String field = VariableConverter.normalizeFieldName(variable.getName());
    BSONObject fileMetadata = (BSONObject) valueObject.get(field);
    if(fileMetadata == null) {
      return TextType.get().nullValue();
    }

    if(variable.isRepeatable()) {
      List<Value> sequenceValues = Lists.newArrayList();
      for(BSONObject occurrenceObj : (Iterable<BSONObject>) fileMetadata) {
        sequenceValues.add(getBinaryMetadata(occurrenceObj));
      }
      return BinaryType.get().sequenceOfReferences(factory, TextType.get().sequenceOf(sequenceValues));
    }
    return BinaryType.get().valueOfReference(factory, getBinaryMetadata(fileMetadata));
  }

  private Value getBinaryMetadata(BSONObject valueObject) {
    if(!valueObject.containsField(GRID_FILE_ID)) {
      return TextType.get().nullValue();
    }
    try {
      JSONObject properties = new JSONObject();
      properties.put(GRID_FILE_ID, valueObject.get(GRID_FILE_ID));
      properties.put(GRID_FILE_SIZE, valueObject.containsField(GRID_FILE_SIZE) ? valueObject.get(GRID_FILE_SIZE) : 0);
      properties.put(GRID_FILE_MD5, valueObject.get(GRID_FILE_MD5));
      return TextType.get().valueOf(properties.toString());
    } catch(JSONException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {
      @Nonnull
      @Override
      public Value getLastUpdate() {
        return getTimestamp("updated");
      }

      @Nonnull
      @Override
      public Value getCreated() {
        return getTimestamp("created");
      }

      private Value getTimestamp(String key) {
        BSONObject timestamps = (BSONObject) getDBObject().get(MongoDBDatasource.TIMESTAMPS_FIELD);
        return ValueConverter.unmarshall(DateTimeType.get(), timestamps.get(key));
      }
    };
  }

  @Nonnull
  private BSONObject getDBObject() {
    if(object == null) {
      DBObject template = BasicDBObjectBuilder.start("_id", entity.getIdentifier()).get();
      object = valueTable.getValueSetCollection().findOne(template);
      if(object == null) {
        throw new NoSuchValueSetException(valueTable, entity);
      }
    }
    return object;
  }

}
