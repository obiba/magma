package org.obiba.magma.datasource.fs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.fs.FsDatasource.InputCallback;
import org.obiba.magma.datasource.fs.FsDatasource.OutputCallback;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.XStream;

class FsVariableEntityProvider implements VariableEntityProvider, Initialisable, Disposable {

  private static final String ENTITIES_NAME = "entities.xml";

  private FsValueTable valueTable;

  private String entityType;

  private XStream xstream;

  private BiMap<VariableEntity, String> entityToFile = HashBiMap.create();

  /** Pads filenames with zeroes */
  private NumberFormat entryFilenameFormat = new DecimalFormat("0000000");

  FsVariableEntityProvider(FsValueTable valueTable) {
    this(valueTable, null);
  }

  FsVariableEntityProvider(FsValueTable valueTable, String entityType) {
    this.valueTable = valueTable;
    this.entityType = entityType;
    this.xstream = valueTable.getDatasource().getXStreamInstance();
  }

  @Override
  public void initialise() {
    valueTable.readEntry(ENTITIES_NAME, new InputCallback<Void>() {
      @Override
      @SuppressWarnings("unchecked")
      public Void readEntry(Reader reader) throws IOException {
        try {
          ObjectInputStream ois = xstream.createObjectInputStream(reader);
          entityType = (String) ois.readObject();
          Map<String, String> entries = (Map<String, String>) ois.readObject();
          for(Map.Entry<String, String> entry : entries.entrySet()) {
            entityToFile.put(new VariableEntityBean(entityType, entry.getKey()), entry.getValue());
          }
        } catch(ClassNotFoundException e) {
          throw new MagmaRuntimeException(e);
        }
        return null;
      }
    });
  }

  @Override
  public void dispose() {
    valueTable.writeEntry(ENTITIES_NAME, new OutputCallback<Void>() {
      @Override
      public Void writeEntry(Writer writer) throws IOException {
        ObjectOutputStream oos = xstream.createObjectOutputStream(writer, "entities");
        oos.writeObject(entityType);
        Map<String, String> entries = Maps.newHashMap();
        for(Map.Entry<VariableEntity, String> entry : entityToFile.entrySet()) {
          entries.put(entry.getKey().getIdentifier(), entry.getValue());
        }
        oos.writeObject(entries);
        oos.close();
        return null;
      }
    });
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    return entityToFile.keySet();
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return getEntityType().equals(entityType);
  }

  String addEntity(VariableEntity entity) {
    if(entityToFile.containsKey(entity) == false) {
      entityToFile.put(entity, entryFilenameFormat.format(entityToFile.size() + 1) + ".xml");
    }
    return getEntityFile(entity);
  }

  String getEntityFile(VariableEntity variableEntity) {
    return entityToFile.get(variableEntity);
  }

  VariableEntity getEntityForFile(String filename) {
    return entityToFile.inverse().get(filename);
  }

}
