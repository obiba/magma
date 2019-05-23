/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.fs;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.XStream;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.fs.FsDatasource.InputCallback;
import org.obiba.magma.datasource.fs.FsDatasource.OutputCallback;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

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
          for (Map.Entry<String, String> entry : entries.entrySet()) {
            entityToFile.put(new VariableEntityBean(entityType, entry.getKey()), entry.getValue());
          }
        } catch (ClassNotFoundException e) {
          throw new MagmaRuntimeException(e);
        }
        return null;
      }
    });
  }

  @Override
  public void dispose() {
    if (entityToFileMapModified || !valueTable.getEntry(ENTITIES_NAME).exists()) {
      valueTable.writeEntry(ENTITIES_NAME, new OutputCallback<Void>() {
        @Override
        public Void writeEntry(Writer writer) throws IOException {
          try (ObjectOutputStream oos = xstream.createObjectOutputStream(writer, "entities")) {
            oos.writeObject(entityType);
            Map<String, String> entries = Maps.newHashMap();
            for (Map.Entry<VariableEntity, String> entry : entityToFile.entrySet()) {
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
  public List<VariableEntity> getVariableEntities() {
    return ImmutableList.copyOf(entityToFile.keySet());
  }

  @Override
  public boolean isForEntityType(@SuppressWarnings("ParameterHidesMemberVariable") String entityType) {
    return getEntityType().equals(entityType);
  }

  String addEntity(VariableEntity entity) {
    if (!entityToFile.containsKey(entity)) {
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
