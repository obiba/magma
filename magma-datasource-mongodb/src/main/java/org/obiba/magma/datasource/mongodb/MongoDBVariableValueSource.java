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

import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoDBVariableValueSource implements VariableValueSource, VectorSource {

  private final MongoDBValueTable table;

  private final String name;

  private MongoDBVariable variable;

  private Value lastUpdated;

  public MongoDBVariableValueSource(MongoDBValueTable table, String name) {
    this.table = table;
    this.name = name;
  }

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  @NotNull
  @Override
  public MongoDBVariable getVariable() {
    Value tableLastUpdate = table.getTimestamps().getLastUpdate();
    if(lastUpdated == null || !lastUpdated.equals(tableLastUpdate)) {
      lastUpdated = tableLastUpdate;
      variable = VariableConverter.unmarshall(table.findVariable(name));
    }
    return variable;
  }

  @NotNull
  @Override
  public ValueType getValueType() {
    return getVariable().getValueType();
  }

  @Override
  public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
    if(entities.isEmpty()) {
      return ImmutableList.of();
    }
    return new Iterable<Value>() {
      @Override
      public Iterator<Value> iterator() {
        return new ValueIterator(getVariable(), entities.iterator());
      }
    };
  }

  @NotNull
  @Override
  public Value getValue(ValueSet valueSet) {
    return ((MongoDBValueSet) valueSet).getValue(getVariable());
  }

  @Override
  public boolean supportVectorSource() {
    return true;
  }

  @NotNull
  @Override
  public VectorSource asVectorSource() {
    return this;
  }

  private class ValueIterator implements Iterator<Value> {

    private final String field;

    private final ValueType type;

    private final boolean repeatable;

    private final DBObject fields;

    private final Iterator<VariableEntity> entities;

    private final DBCursor cursor;

    private final Map<String, Value> valueMap = Maps.newHashMap();

    private ValueIterator(MongoDBVariable variable, Iterator<VariableEntity> entities) {
      field = variable.getId();
      type = variable.getValueType();
      repeatable = variable.isRepeatable();
      fields = BasicDBObjectBuilder.start(field, 1).get();
      this.entities = entities;
      cursor = table.getValueSetCollection().find(new BasicDBObject(), fields);
    }

    @Override
    public boolean hasNext() {
      return entities.hasNext();
    }

    @Override
    public Value next() {
      VariableEntity entity = entities.next();

      if(valueMap.containsKey(entity.getIdentifier())) return valueMap.get(entity.getIdentifier());

      boolean found = false;
      while(cursor.hasNext() && !found) {
        DBObject obj = cursor.next();
        String id = obj.get("_id").toString();
        valueMap.put(id, ValueConverter.unmarshall(type, repeatable, field, obj));
        found = id.equals(entity.getIdentifier());
      }

      if(valueMap.containsKey(entity.getIdentifier())) return valueMap.get(entity.getIdentifier());
      return ValueConverter.unmarshall(type, repeatable, field, null);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
