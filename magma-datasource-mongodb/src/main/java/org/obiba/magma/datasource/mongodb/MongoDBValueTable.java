package org.obiba.magma.datasource.mongodb;

import java.util.Date;
import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityProvider;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoDBValueTable extends AbstractValueTable {

  public MongoDBValueTable(@Nonnull MongoDBDatasource datasource, @Nonnull String name) {
    this(datasource, name, null);
  }

  public MongoDBValueTable(@Nonnull MongoDBDatasource datasource, @Nonnull String name, @Nonnull String entityType) {
    super(datasource, name);
    setVariableEntityProvider(new MongoDBVariableEntityProvider(this, entityType));
  }

  DBCollection getValueTableCollection() {
    return ((MongoDBDatasource)getDatasource()).getValueTableCollection();
  }

  DBObject asDBObject() {
    DBObject tableObject = getValueTableCollection().findOne(BasicDBObjectBuilder.start().add("name",getName()).get());
    if(tableObject == null) {
      DBObject timestamps = BasicDBObjectBuilder.start().add("created", new Date()).add("updated", new Date()).get();
      tableObject = BasicDBObjectBuilder.start().add("name", getName()).add("entityType", getEntityType())
          .add("timestamps", timestamps).get();
      getValueTableCollection().insert(tableObject);
    }
    return tableObject;
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
    return null;
  }

}
