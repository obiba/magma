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

import com.google.common.collect.Lists;
import com.mongodb.DBObject;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.magma.*;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.obiba.magma.datasource.mongodb.MongoDBValueTableWriter.GRID_FILE_ID;
import static org.obiba.magma.datasource.mongodb.MongoDBValueTableWriter.GRID_FILE_SIZE;

class MongoDBValueSet implements ValueSet {

  private final MongoDBValueTable valueTable;

  private final VariableEntity entity;

  private Document object;

  private final MongoDBValueSetFetcher fetcher;

  MongoDBValueSet(MongoDBValueTable valueTable, VariableEntity entity) {
    this.valueTable = valueTable;
    this.entity = entity;
    this.fetcher = new MongoDBValueSetFetcher(valueTable);
  }

  @Override
  public ValueTable getValueTable() {
    return valueTable;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return entity;
  }

  Value getValue(MongoDBVariable variable) {
    Document valueObject = getDBObject();
    ValueType valueType = variable.getValueType();
    return valueType.equals(BinaryType.get())
        ? getBinaryValue(variable, valueObject)
        : ValueConverter.unmarshall(variable, valueObject);
  }

  @SuppressWarnings("unchecked")
  private Value getBinaryValue(MongoDBVariable variable, Document valueObject) {
    return getBinaryValue(valueTable.getMongoDBFactory(), variable, valueObject);
  }

  public static Value getBinaryValue(MongoDBFactory mongoDBFactory, MongoDBVariable variable, Document valueObject) {
    ValueLoaderFactory factory = new MongoDBValueLoaderFactory(mongoDBFactory);

    Object fileMetadata = valueObject.get(variable.getId());
    if(fileMetadata == null) {
      return BinaryType.get().nullValue();
    }

    if(variable.isRepeatable()) {
      List<Value> sequenceValues = Lists.newArrayList();
      for(Document occurrenceObj : (Iterable<Document>) fileMetadata) {
        sequenceValues.add(getBinaryMetadata(occurrenceObj));
      }
      return BinaryType.get().sequenceOfReferences(factory, TextType.get().sequenceOf(sequenceValues));
    }
    return BinaryType.get().valueOfReference(factory, getBinaryMetadata((Document) fileMetadata));
  }

  private static Value getBinaryMetadata(DBObject valueObject) {
    if(!valueObject.containsField(GRID_FILE_ID)) {
      return TextType.get().nullValue();
    }
    try {
      JSONObject properties = new JSONObject();
      properties.put(GRID_FILE_ID, valueObject.get(GRID_FILE_ID));
      properties.put(GRID_FILE_SIZE, valueObject.containsField(GRID_FILE_SIZE) ? valueObject.get(GRID_FILE_SIZE) : Integer.valueOf(0));
      return TextType.get().valueOf(properties.toString());
    } catch(JSONException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  private static Value getBinaryMetadata(Document valueObject) {
    if(!valueObject.containsKey(GRID_FILE_ID)) {
      return TextType.get().nullValue();
    }
    try {
      JSONObject properties = new JSONObject();
      properties.put(GRID_FILE_ID, valueObject.get(GRID_FILE_ID));
      properties.put(GRID_FILE_SIZE, valueObject.containsKey(GRID_FILE_SIZE) ? valueObject.get(GRID_FILE_SIZE) : Integer.valueOf(0));
      return TextType.get().valueOf(properties.toString());
    } catch(JSONException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {
      @NotNull
      @Override
      public Value getLastUpdate() {
        return getTimestamp("updated");
      }

      @NotNull
      @Override
      public Value getCreated() {
        return getTimestamp("created");
      }

      private Value getTimestamp(String key) {
        Document timestamps = (Document) getDBObject().get(MongoDBDatasource.TIMESTAMPS_FIELD);
        return ValueConverter.unmarshall(DateTimeType.get(), timestamps.get(key));
      }
    };
  }

  @NotNull
  private Document getDBObject() {
    if(object == null) {
      object = fetcher.getDBObject(entity);
      if(object == null) {
        throw new NoSuchValueSetException(valueTable, entity);
      }
    }
    return object;
  }

  void setDBObject(Document object) {
    this.object = object;
  }

}
