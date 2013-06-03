package org.obiba.magma.datasource.mongodb;

import java.util.Set;

import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;

import com.google.common.collect.ImmutableSet;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class MongoDBVariableValueSourceFactory implements VariableValueSourceFactory {

  private MongoDBValueTable table;

  public MongoDBVariableValueSourceFactory(MongoDBValueTable table) {
    this.table = table;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    DBObject object = table.getValueTableCollection()
        .findOne(BasicDBObjectBuilder.start().add("name", table.getName()).get());
    Object o = object.get("variables");

    ImmutableSet.Builder<VariableValueSource> builder = ImmutableSet.builder();

    return builder.build();
  }
}
