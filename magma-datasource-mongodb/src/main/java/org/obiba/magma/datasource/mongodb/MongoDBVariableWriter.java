package org.obiba.magma.datasource.mongodb;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.mongodb.converter.VariableConverter;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

class MongoDBVariableWriter implements ValueTableWriter.VariableWriter {

  private final DBObject tableObject;

  MongoDBVariableWriter(@Nonnull DBObject tableObject) {
    this.tableObject = tableObject;
  }

  @Override
  public void writeVariable(@Nonnull Variable variable) {
    BasicDBList variables = (BasicDBList)tableObject.get("variables");
    if (variables == null) {
      variables = new BasicDBList();
    }
    variables.add(VariableConverter.marshall(variable));

    tableObject.put("variables",variables);
  }

  @Override
  public void close() throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
