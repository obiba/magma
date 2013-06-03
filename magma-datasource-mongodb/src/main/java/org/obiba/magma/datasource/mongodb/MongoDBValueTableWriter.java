package org.obiba.magma.datasource.mongodb;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.VariableEntity;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

class MongoDBValueTableWriter implements ValueTableWriter {

  private final MongoDBValueTable table;

  private DBObject tableObject;

  MongoDBValueTableWriter(MongoDBValueTable table) {
    this.table = table;
  }

  @Override
  public VariableWriter writeVariables() {
    return new MongoDBVariableWriter(getTableObject());
  }

  @Nonnull
  @Override
  public ValueSetWriter writeValueSet(@Nonnull VariableEntity entity) {
    return null;
  }

  private DBObject getTableObject() {
    if (tableObject == null) {
      tableObject = table.asDBObject();
    }
    return tableObject;
  }

  @Override
  public void close() throws IOException {
    if(tableObject != null) {
      DBObject timestamps = (DBObject) tableObject.get("timestamps");
      timestamps.put("updated", new Date());
      // insert or update
      table.getValueTableCollection().save(tableObject);
    }
  }

}
