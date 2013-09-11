package org.obiba.magma.datasource.mongodb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueLoader;
import org.obiba.magma.ValueLoaderFactory;

import com.mongodb.gridfs.GridFSDBFile;

public class MongoDBValueLoaderFactory implements ValueLoaderFactory {

  private final MongoDBFactory mongoDBFactory;

  public MongoDBValueLoaderFactory(MongoDBFactory mongoDBFactory) {
    this.mongoDBFactory = mongoDBFactory;
  }

  @Override
  public ValueLoader create(Value valueRef, @Nullable Integer occurrence) {
    return new MongoDBBinaryValueLoader(mongoDBFactory, valueRef);
  }

  private static final class MongoDBBinaryValueLoader implements ValueLoader {

    private static final long serialVersionUID = -5992432763907068814L;

    private final MongoDBFactory mongoDBFactory;

    private final Value valueRef;

    private byte[] value;

    private MongoDBBinaryValueLoader(MongoDBFactory mongoDBFactory, Value valueRef) {
      this.mongoDBFactory = mongoDBFactory;
      this.valueRef = valueRef;
    }

    @Override
    public boolean isNull() {
      return valueRef == null || valueRef.isNull();
    }

    @Nonnull
    @Override
    public Object getValue() {
      if(value == null) {
        ObjectId fileId = null;
        JSONObject properties = new JSONObject(valueRef.getValue());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
          fileId = ObjectId.massageToObjectId(properties.get("_id"));
          GridFSDBFile file = mongoDBFactory.getGridFS().findOne(fileId);
          file.writeTo(outputStream);
          value = outputStream.toByteArray();
        } catch(IOException e) {
          throw new MagmaRuntimeException("Cannot retrieve content of gridFsFile [" + fileId + "]", e);
        } catch(JSONException e) {
          throw new MagmaRuntimeException("Cannot retrieve grid file Id for " + valueRef.getValue(), e);
        } finally {
          try {
            outputStream.close();
          } catch(IOException ignored) {
          }
        }
      }
      return value;
    }

    @Override
    public long getLength() {
      try {
        JSONObject properties = new JSONObject(valueRef.getValue());
        return properties.has("size") ? properties.getLong("size") : 0;
      } catch(JSONException e) {
        throw new MagmaRuntimeException("Cannot retrieve grid file size for " + valueRef.getValue(), e);
      }
    }

  }

}
