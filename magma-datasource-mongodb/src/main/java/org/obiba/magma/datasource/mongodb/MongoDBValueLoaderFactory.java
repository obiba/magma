package org.obiba.magma.datasource.mongodb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueLoader;
import org.obiba.magma.ValueLoaderFactory;

import com.mongodb.gridfs.GridFSDBFile;

import static org.obiba.magma.datasource.mongodb.MongoDBValueTableWriter.GRID_FILE_ID;
import static org.obiba.magma.datasource.mongodb.MongoDBValueTableWriter.GRID_FILE_SIZE;

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

    @NotNull
    @Override
    public Object getValue() {
      if(value == null) {
        String json = (String) valueRef.getValue();
        try {
          JSONObject jsonObject = new JSONObject(json);
          if(jsonObject.has(GRID_FILE_ID)) {
            value = getByteArray(jsonObject.getString(GRID_FILE_ID));
          }
        } catch(JSONException e) {
          throw new MagmaRuntimeException("Cannot retrieve grid file Id for " + json, e);
        }
      }
      return value;
    }

    private byte[] getByteArray(String fileId) {
      try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        GridFSDBFile file = mongoDBFactory.getGridFS().findOne(new ObjectId(fileId));
        file.writeTo(outputStream);
        return outputStream.toByteArray();
      } catch(IOException e) {
        throw new MagmaRuntimeException("Cannot retrieve content of gridFsFile [" + fileId + "]", e);
      }
    }

    @Override
    public long getLength() {
      if(valueRef.isNull()) return 0;
      try {
        JSONObject properties = new JSONObject((String) valueRef.getValue());
        return properties.has(GRID_FILE_SIZE) ? properties.getLong(GRID_FILE_SIZE) : 0;
      } catch(JSONException e) {
        throw new MagmaRuntimeException("Cannot retrieve grid file size for " + valueRef.getValue(), e);
      }
    }

  }

}
