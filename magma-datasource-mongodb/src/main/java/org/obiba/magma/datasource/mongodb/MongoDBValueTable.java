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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.BSONObject;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.type.DateTimeType;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

public class MongoDBValueTable extends AbstractValueTable {

  static final String VARIABLE_SUFFIX = "_variable";

  static final String VALUESET_SUFFIX = "_value_set";

  static final String TIMESTAMPS_FIELD = "_timestamps";

  static final String TIMESTAMPS_CREATED_FIELD = "created";

  static final String TIMESTAMPS_UPDATED_FIELD = "updated";

  public MongoDBValueTable(@Nonnull Datasource datasource, @Nonnull String name) {
    this(datasource, name, null);
  }

  public MongoDBValueTable(@Nonnull Datasource datasource, @Nonnull String name, @Nullable String entityType) {
    super(datasource, name);
    setVariableEntityProvider(new MongoDBVariableEntityProvider(this, entityType));
    // ensure corresponding document is stored
    asDBObject();
  }

  ValueTableWriter.VariableWriter createVariableWriter() {
    return new MongoDBVariableWriter();
  }

  ValueTableWriter.ValueSetWriter createValueSetWriter(@Nonnull VariableEntity entity) {
    return new MongoDBValueSetWriter(entity);
  }

  DBCollection getValueTableCollection() {
    return ((MongoDBDatasource) getDatasource()).getValueTableCollection();
  }

  DBCollection getVariablesCollection() {
    return ((MongoDBDatasource) getDatasource()).getDB().getCollection(getId() + VARIABLE_SUFFIX);
  }

  DBCollection getValueSetCollection() {
    return ((MongoDBDatasource) getDatasource()).getDB().getCollection(getId() + VALUESET_SUFFIX);
  }

  DBObject asDBObject() {
    DBObject tableObject = getValueTableCollection().findOne(BasicDBObjectBuilder.start() //
        .add("_id", getId()) //
        .get());

    if(tableObject == null) {
      tableObject = BasicDBObjectBuilder.start() //
          .add("_id", getId()) //
          .add("datasource", getDatasource().getName()) //
          .add("name", getName()) //
          .add("entityType", getEntityType()) //
          .add(TIMESTAMPS_FIELD, createTimestampsObject()).get();
      getValueTableCollection().insert(tableObject, WriteConcern.ACKNOWLEDGED);
    }

    return tableObject;
  }

  /**
   * See <a href="http://docs.mongodb.org/manual/reference/limits/">MongoDB Limits and Thresholds</a>.
   *
   * @return
   */
  private String getId() {
    String norm = (getDatasource().getName() + "." + getName()).replaceAll("[$]", "_");
    return norm.startsWith("system") ? "_" + norm.substring(6) : norm;
  }

  private DBObject createTimestampsObject() {
    return BasicDBObjectBuilder.start().add(TIMESTAMPS_CREATED_FIELD, new Date())
        .add(TIMESTAMPS_UPDATED_FIELD, new Date()).get();
  }

  @Override
  public void initialise() {
    addVariableValueSources(new MongoDBVariableValueSourceFactory(this));
    super.initialise();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new MongoDBValueSet(this, entity);
  }

  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {

      private BSONObject getTimestampsObject() {
        return (BSONObject) asDBObject().get(TIMESTAMPS_FIELD);
      }

      @Nonnull
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(getTimestampsObject().get("updated"));
      }

      @Nonnull
      @Override
      public Value getCreated() {
        return DateTimeType.get().valueOf(getTimestampsObject().get("created"));
      }
    };
  }

  public void drop() {
    // drop associated collections
    getValueSetCollection().drop();
    getVariablesCollection().drop();
    getValueTableCollection().remove(BasicDBObjectBuilder.start().add("_id", getId()).get());
  }

  private class MongoDBValueSetWriter implements ValueTableWriter.ValueSetWriter {

    private final VariableEntity entity;

    private DBObject valueSetObject;

    private MongoDBValueSetWriter(VariableEntity entity) {
      this.entity = entity;
    }

    private BSONObject getValueSetObject() {
      if(valueSetObject == null) {
        DBObject template = BasicDBObjectBuilder.start("_id", entity.getIdentifier()).get();
        valueSetObject = getValueSetCollection().findOne(template);
        if(valueSetObject == null) {
          valueSetObject = template;
          valueSetObject.put(TIMESTAMPS_FIELD, createTimestampsObject());
        }
      }
      return valueSetObject;
    }

    @Override
    public void writeValue(@Nonnull Variable variable, Value value) {
      String field = VariableConverter.normalizeFieldName(variable.getName());
      getValueSetObject().put(field, ValueConverter.marshall(variable, value));
    }

    @Override
    public void close() throws IOException {
      BSONObject timestamps = (BSONObject) getValueSetObject().get(TIMESTAMPS_FIELD);
      timestamps.put(TIMESTAMPS_UPDATED_FIELD, new Date());
      getValueSetCollection().save(valueSetObject);
    }
  }

  private class MongoDBVariableWriter implements ValueTableWriter.VariableWriter {
    @Override
    public void writeVariable(@Nonnull Variable variable) {
      if(getVariablesCollection().findOne(BasicDBObjectBuilder.start("_id", variable.getName()).get()) == null) {
        addVariableValueSource(new MongoDBVariableValueSource(MongoDBValueTable.this, variable.getName()));
      }
      // insert or update
      DBObject varObject = VariableConverter.marshall(variable);
      getVariablesCollection().save(varObject);
    }

    @Override
    public void removeVariable(@Nonnull Variable variable) {
      DBCollection variablesCollection = getVariablesCollection();
      DBObject varObj = variablesCollection.findOne(BasicDBObjectBuilder.start("_id", variable.getName()).get());
      if(varObj == null) return;

      // remove from the variable collection
      removeVariableValueSource(variable.getName());
      variablesCollection.remove(varObj);
      // remove associated values from the value set sollection
      removeVariableValues(variable);
    }

    private void removeVariableValues(@Nonnull Variable variable) {
      DBCollection valueSetCollection = getValueSetCollection();
      DBCursor cursor = valueSetCollection.find();
      String field = VariableConverter.normalizeFieldName(variable.getName());
      while(cursor.hasNext()) {
        DBObject obj = cursor.next();
        // TODO enough to remove a binary file?
        obj.removeField(field);
        BSONObject timestamps = (BSONObject) obj.get(TIMESTAMPS_FIELD);
        timestamps.put(TIMESTAMPS_UPDATED_FIELD, new Date());
        valueSetCollection.save(obj);
      }
    }

    @Override
    public void close() throws IOException {

    }
  }
}
