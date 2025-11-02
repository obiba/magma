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
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.obiba.magma.*;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;
import org.obiba.magma.type.BinaryType;

import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MongoDBVariableValueSource implements VariableValueSource, VectorSource {

  private final MongoDBValueTable table;

  private final String name;

  private MongoDBVariable variable;

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
  public synchronized MongoDBVariable getVariable() {
    if (variable == null) {
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
  public Iterable<Value> getValues(final Iterable<VariableEntity> entities) {
    if (!entities.iterator().hasNext()) {
      return ImmutableList.of();
    }
    return () -> new ValueIterator(getVariable(), entities);
  }

  @NotNull
  @Override
  public Value getValue(ValueSet valueSet) {
    ValueSet vs = valueSet;
    if (valueSet instanceof ValueSetWrapper) {
      vs = ((ValueSetWrapper) valueSet).getWrapped();
    }
    return ((MongoDBValueSet) vs).getValue(getVariable());
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

  void invalidate() {
    // reset the cached variable object
    variable = null;
  }

  private class ValueIterator implements Iterator<Value> {

    private final String field;

    private final ValueType type;

    private final boolean repeatable;

    private final DBObject fields;

    private final Iterator<VariableEntity> entities;

    private final List<List<String>> identifiersPartitions;

    private int partitionIndex = 0;

    private final Map<String, Value> valueMap = Maps.newHashMap();

    private MongoCursor<Document> cursor;

    private ValueIterator(MongoDBVariable variable, Iterable<VariableEntity> entities) {
      field = variable.getId();
      type = variable.getValueType();
      repeatable = variable.isRepeatable();
      fields = BasicDBObjectBuilder.start(field, 1).get();
      this.entities = entities.iterator();
      this.identifiersPartitions = Lists.partition(StreamSupport.stream(entities.spliterator(), false)
              .map(VariableEntity::getIdentifier).collect(Collectors.toList()),
          table.getVariableEntityBatchSize());
    }

    @Override
    public boolean hasNext() {
      return entities.hasNext();
    }

    @Override
    public Value next() {
      VariableEntity entity = entities.next();

      if (valueMap.containsKey(entity.getIdentifier())) return getValueFromMap(entity);

      if (cursor == null || !cursor.hasNext()) {
        cursor = newCursor();
      }

      if (cursor != null) {
        boolean found = false;
        while (cursor.hasNext() && !found) {
          Document obj = cursor.next();
          String id = obj.get("_id").toString();
          Value value = variable.getValueType().equals(BinaryType.get())
              ? getBinaryValue(obj)
              : ValueConverter.unmarshall(type, repeatable, field, obj);
          valueMap.put(id, value);
          found = id.equals(entity.getIdentifier());
        }
      }

      if (valueMap.containsKey(entity.getIdentifier())) return getValueFromMap(entity);
      return ValueConverter.unmarshall(type, repeatable, field, null);
    }

    private MongoCursor<Document> newCursor() {
      if (partitionIndex < identifiersPartitions.size()) {
        List<String> identifiers = identifiersPartitions.get(partitionIndex);
        partitionIndex++;
        return table.getValueSetCollection()
            .find(Filters.in("_id", identifiers))
            .projection(Projections.include(field))
            .cursor();
      }
      return null;
    }

    private Value getBinaryValue(Document valueObject) {
      return MongoDBValueSet.getBinaryValue(table.getMongoDBFactory(), variable, valueObject);
    }

    /**
     * No duplicate of entities, so remove value from map once get.
     *
     * @param entity
     * @return
     */
    private Value getValueFromMap(VariableEntity entity) {
      Value value = valueMap.get(entity.getIdentifier());
      valueMap.remove(entity.getIdentifier());
      return value;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
