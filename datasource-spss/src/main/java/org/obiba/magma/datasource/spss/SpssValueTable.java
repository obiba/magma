/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.spss.support.SpssDatasourceParsingException;
import org.obiba.magma.datasource.spss.support.SpssVariableValueSourceFactory;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.DateTimeType;
import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

import com.google.common.collect.ImmutableSet;

public class SpssValueTable extends AbstractValueTable {

  private final SPSSFile spssFile;

  public SpssValueTable(Datasource datasource, String name, String entityType, SPSSFile spssFile) {
    super(datasource, name);
    this.spssFile = spssFile;
    setVariableEntityProvider(new SpssVariableEntityProvider(entityType));
  }

  @Override
  public void initialise() {
    initializeVariableSources();
    super.initialise();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new SpssValueSet(this, entity, spssFile);
  }

  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {
      @Override
      public Value getLastUpdate() {
        Date lastModified = new Date(spssFile.file.lastModified());
        return DateTimeType.get().valueOf(lastModified);
      }

      @Override
      public Value getCreated() {
        // Not currently possible to read a file creation timestamp. Coming in JDK 7 NIO.
        return DateTimeType.get().nullValue();
      }
    };
  }

  //
  // Private methods
  //

  private void initializeVariableSources() {
    loadMetadata();
    addVariableValueSources(new SpssVariableValueSourceFactory(spssFile));
  }

  private void loadMetadata() {
    if(spssFile.isMetadataLoaded) {
      return;
    }

    try {
      spssFile.loadMetadata();
    } catch(Exception e) {
      throw new SpssDatasourceParsingException(e, "SpssFailedToLoadMetadata", spssFile.file.getName());
    }
  }

  private void loadData() {
    if(spssFile.isDataLoaded) {
      return;
    }

    try {
      if(!spssFile.isMetadataLoaded) {
        loadMetadata();
      }

      spssFile.loadData();
    } catch(Exception e) {
      throw new SpssDatasourceParsingException(e, "SpssFailedToLoadData", spssFile.file.getName());
    }
  }

  //
  // Inner Classes
  //

  private class SpssVariableEntityProvider implements VariableEntityProvider {

    private final String entityType;

    private Set<VariableEntity> variableEntities;

    private SpssVariableEntityProvider(String entityType) {
      this.entityType = entityType == null || entityType.trim().isEmpty() ? "Participant" : entityType.trim();
    }

    @Override
    public String getEntityType() {
      return entityType;
    }

    @Override
    public boolean isForEntityType(String anEntityType) {
      return getEntityType().equals(anEntityType);
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {

      if(variableEntities == null) {
        loadData();

        Set<String> entityIdentifiers = new HashSet<String>();
        ImmutableSet.Builder<VariableEntity> entitiesBuilder = ImmutableSet.builder();
        SPSSVariable entityVariable = spssFile.getVariable(0);
        int numberOfObservations = entityVariable.getNumberOfObservation();

        try {

          for(int i = 1; i <= numberOfObservations; i++) {
            String identifier = entityVariable.getValueAsString(i, new FileFormatInfo(FileFormatInfo.Format.ASCII));

            if (entityIdentifiers.contains(identifier)) {
              throw new SpssDatasourceParsingException("Duplicated entity identifier", "SpssDuplicateEntity", getName(), i, identifier);
            }

            entitiesBuilder.add(new SpssVariableEntity(entityType, identifier, i));
            entityIdentifiers.add(identifier);
          }

          variableEntities = entitiesBuilder.build();

        } catch(SPSSFileException e) {
          throw new SpssDatasourceParsingException(e, "TableDefinitionErrors", getName());
        }
      }

      return variableEntities;
    }

  }

}
