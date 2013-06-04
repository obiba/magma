package org.obiba.magma.datasource.mongodb;

import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;

import com.google.common.collect.ImmutableSet;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoDBVariableValueSourceFactory implements VariableValueSourceFactory {

  private MongoDBValueTable table;

  public MongoDBVariableValueSourceFactory(MongoDBValueTable table) {
    this.table = table;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    ImmutableSet.Builder<VariableValueSource> builder = ImmutableSet.builder();
    DBCursor cursor = table.getVariablesCollection()
        .find(new BasicDBObject(), BasicDBObjectBuilder.start("_id", 1).get());
    try {
      while(cursor.hasNext()) {
        String name = cursor.next().get("_id").toString();
        builder.add(new MongoDBVariableValueSource(table, name));
      }
    } finally {
      cursor.close();
    }

    return builder.build();
  }

  public static class MongoDBVariableValueSource implements VariableValueSource {

    private final MongoDBValueTable table;

    private final String name;

    private Variable variable;

    public MongoDBVariableValueSource(MongoDBValueTable table, String name) {
      this.table = table;
      this.name = name;
    }

    @Override
    public Variable getVariable() {
      if(variable == null) {
        DBObject template = BasicDBObjectBuilder.start("_id", name).get();
        variable = VariableConverter.unmarshall(table.getVariablesCollection().findOne(template));
      }
      return variable;
    }

    @Nonnull
    @Override
    public ValueType getValueType() {
      return getVariable().getValueType();
    }

    @Nonnull
    @Override
    public Value getValue(ValueSet valueSet) {
      // TODO
      return null;
    }

    @Nullable
    @Override
    public VectorSource asVectorSource() {

      return new VectorSource() {
        @Override
        public ValueType getValueType() {
          return getVariable().getValueType();
        }

        @Override
        public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
          // TODO
          return null;
        }
      };
    }
  }
}
