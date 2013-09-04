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

import javax.annotation.Nonnull;

import org.bson.BSONObject;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.type.DateTimeType;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

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

  public Value getValue(Variable variable) {
    return ValueConverter.unmarshall(variable, getDBObject());
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
        BSONObject timestamps = (BSONObject) getDBObject().get(MongoDBValueTable.TIMESTAMPS_FIELD);
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
