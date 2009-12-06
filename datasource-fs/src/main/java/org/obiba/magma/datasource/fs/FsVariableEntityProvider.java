package org.obiba.magma.datasource.fs;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.fs.FsDatasource.InputCallback;
import org.obiba.magma.datasource.fs.FsDatasource.OutputCallback;
import org.obiba.magma.support.VariableEntityProvider;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.thoughtworks.xstream.XStream;

class FsVariableEntityProvider implements VariableEntityProvider, Initialisable, Disposable {

  private static final String ENTITIES_NAME = "entities.xml";

  private FsValueTable valueTable;

  private String entityType;

  private BiMap<VariableEntity, String> entityToFile = HashBiMap.create();

  FsVariableEntityProvider(FsValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Override
  public void initialise() {
    valueTable.readEntry(ENTITIES_NAME, new InputCallback<Void>() {
      @Override
      @SuppressWarnings("unchecked")
      public Void readEntry(Reader reader) throws IOException {
        XStream xstream = new XStream();
        Map<VariableEntity, String> entries = (Map<VariableEntity, String>) xstream.fromXML(reader);
        entityToFile.putAll(entries);
        return null;
      }
    });
  }

  @Override
  public void dispose() {
    valueTable.writeEntry(ENTITIES_NAME, new OutputCallback<Void>() {
      @Override
      public Void writeEntry(Writer writer) throws IOException {
        XStream xstream = new XStream();
        xstream.toXML(new HashMap<VariableEntity, String>(entityToFile), writer);
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
    return this.entityType.equals(entityType);
  }

  String addEntity(VariableEntity entity) {
    if (entityToFile.containsKey(entity) == false) {
      entityToFile.put(entity, Integer.toString(entityToFile.size() + 1) + ".xml");
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
