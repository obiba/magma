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

import javax.validation.constraints.NotNull;

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

  private final FsValueTable valueTable;

  private String entityType;

  private final XStream xstream;

  private final BiMap<VariableEntity, String> entityToFile = HashBiMap.create();

  private boolean entityToFileMapModified = false;

  /**
   * Pads filenames with zeroes
   */
  private final NumberFormat entryFilenameFormat = new DecimalFormat("0000000");

  FsVariableEntityProvider(FsValueTable valueTable) {
    this(valueTable, null);
  }

  FsVariableEntityProvider(FsValueTable valueTable, String entityType) {
    this.valueTable = valueTable;
    this.entityType = entityType;
    xstream = valueTable.getDatasource().getXStreamInstance();
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
    if(entityToFileMapModified || !valueTable.getEntry(ENTITIES_NAME).exists()) {
      valueTable.writeEntry(ENTITIES_NAME, new OutputCallback<Void>() {
        @Override
        public Void writeEntry(Writer writer) throws IOException {
          try(ObjectOutputStream oos = xstream.createObjectOutputStream(writer, "entities")) {
            oos.writeObject(entityType);
            Map<String, String> entries = Maps.newHashMap();
            for(Map.Entry<VariableEntity, String> entry : entityToFile.entrySet()) {
              entries.put(entry.getKey().getIdentifier(), entry.getValue());
            }
            oos.writeObject(entries);
            return null;
          }
        }
      });
    }
  }

  @NotNull
  @Override
  public String getEntityType() {
    return entityType;
  }

  @NotNull
  @Override
  public Set<VariableEntity> getVariableEntities() {
    return entityToFile.keySet();
  }

  @Override
  public boolean isForEntityType(@SuppressWarnings("ParameterHidesMemberVariable") String entityType) {
    return getEntityType().equals(entityType);
  }

  String addEntity(VariableEntity entity) {
    if(!entityToFile.containsKey(entity)) {
      entityToFile.put(entity, entryFilenameFormat.format(entityToFile.size() + 1) + ".xml");
      entityToFileMapModified = true;
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
