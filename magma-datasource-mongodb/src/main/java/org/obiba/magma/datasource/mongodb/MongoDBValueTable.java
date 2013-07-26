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
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.DateTimeType;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoDBValueTable extends AbstractValueTable {

  static final String VARIABLE_SUFFIX = "_variable";

  static final String VALUESET_SUFFIX = "_value_set";

  static final String TIMESTAMPS_FIELD = "_timestamps";

  public MongoDBValueTable(@Nonnull MongoDBDatasource datasource, @Nonnull String name) {
    this(datasource, name, null);
  }

  public MongoDBValueTable(@Nonnull MongoDBDatasource datasource, @Nonnull String name, @Nonnull String entityType) {
    super(datasource, name);
    setVariableEntityProvider(new MongoDBVariableEntityProvider(this, entityType));
  }

  ValueTableWriter.VariableWriter createVariableWriter() {
    return new MongoDBVariableWriter();
  }

  ValueTableWriter.ValueSetWriter createValueSetWriter(@Nonnull final VariableEntity entity) {
    return new MongoDBValueSetWriter(entity);
  }

  DBCollection getValueTableCollection() {
    return ((MongoDBDatasource) getDatasource()).getValueTableCollection();
  }

  DBCollection getVariablesCollection() {
    return ((MongoDBDatasource) getDatasource()).getDB().getCollection(normalizeCollectionName(getName() + VARIABLE_SUFFIX));
  }

  DBCollection getValueSetCollection() {
    return ((MongoDBDatasource) getDatasource()).getDB().getCollection(normalizeCollectionName(getName() + VALUESET_SUFFIX));
  }

  DBObject asDBObject() {
    DBObject tableObject = getValueTableCollection().findOne(BasicDBObjectBuilder.start().add("name", getName()).get());
    if(tableObject == null) {
      tableObject = BasicDBObjectBuilder.start().add("name", getName()).add("entityType", getEntityType())
          .add(TIMESTAMPS_FIELD, createTimestampsObject()).get();
      getValueTableCollection().insert(tableObject);
    }
    return tableObject;
  }

  private String normalizeCollectionName(String name) {
    String norm = name.replaceAll("[$]","_");
    return norm.startsWith("system") ? "_" + norm.substring(6) : norm;
  }

  private DBObject createTimestampsObject() {
    return BasicDBObjectBuilder.start().add("created", new Date()).add("updated", new Date()).get();
  }

  @Override
  public void initialise() {
    addVariableValueSources(new MongoDBVariableValueSourceFactory(this));
    super.initialise();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return null;
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
      getValueSetObject().put(field, ValueConverter.marshall(variable,value));
    }

    @Override
    public void close() throws IOException {
      if(valueSetObject != null) {
        BSONObject timestamps = (BSONObject) valueSetObject.get(TIMESTAMPS_FIELD);
        timestamps.put("updated", new Date());
        getValueSetCollection().save(valueSetObject);
      }
    }
  }

  private class MongoDBVariableWriter implements ValueTableWriter.VariableWriter {
    @Override
    public void writeVariable(@Nonnull Variable variable) {
      if (getVariablesCollection().findOne(BasicDBObjectBuilder.start("_id",variable.getName()).get()) == null) {
        // TODO
        addVariableValueSource(new MongoDBVariableValueSourceFactory.MongoDBVariableValueSource(MongoDBValueTable.this, variable.getName()));
      }
      // _id is variable name
      DBObject varObject = VariableConverter.marshall(variable);
      // insert or update
      getVariablesCollection().save(varObject);
    }

    @Override
    public void close() throws IOException {

    }
  }
}
