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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.spss.support.SpssVariableTypeMapper;
import org.obiba.magma.datasource.spss.support.SpssVariableValueFactory;
import org.obiba.magma.datasource.spss.support.SpssVariableValueSourceFactory;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.DateTimeType;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class SpssValueTable extends AbstractValueTable implements Disposable {

  private final static Logger log = LoggerFactory.getLogger(SpssValueTable.class);

  private final SPSSFile spssFile;

  private final String locale;

  public SpssValueTable(Datasource datasource, String name, String entityType, String locale, SPSSFile spssFile) {
    super(datasource, name);
    this.spssFile = spssFile;
    this.locale = locale;
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
      @Nonnull
      @Override
      public Value getLastUpdate() {
        Date lastModified = new Date(spssFile.file.lastModified());
        return DateTimeType.get().valueOf(lastModified);
      }

      @Nonnull
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
    addVariableValueSources(new SpssVariableValueSourceFactory(spssFile, getEntityType(), locale));
  }

  private void loadMetadata() {
    if(spssFile.isMetadataLoaded) {
      return;
    }

    try {
      spssFile.loadMetadata();
    } catch(Exception e) {
      throw new DatasourceParsingException(e.getMessage(), "SpssFailedToLoadMetadata", spssFile.file.getName());
    }
  }

  @Override
  public void dispose() {
    if (spssFile != null) {
      try {
        spssFile.close();
      } catch(IOException e) {
        log.warn("Error occured while closing SPSS file: {}", e.getMessage());
      }
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

        Collection<String> entityIdentifiers = new HashSet<String>();
        ImmutableSet.Builder<VariableEntity> entitiesBuilder = ImmutableSet.builder();
        SPSSVariable entityVariable = spssFile.getVariable(0);
        int numberOfObservations = entityVariable.getNumberOfObservations();
        ValueType valueType = SpssVariableTypeMapper.map(entityVariable);

        for(int i = 1; i <= numberOfObservations; i++) {
          Value identifierValue = new SpssVariableValueFactory(i, entityVariable, valueType).create();

          if (identifierValue.isNull()) {
            throw new DatasourceParsingException("Invalid entity identifier", "SpssEmptyIdentifier",
                entityVariable.getName(), i);
          }

          String identifier = identifierValue.getValue().toString();

          if(entityIdentifiers.contains(identifier)) {
            throw new DatasourceParsingException("Duplicated entity identifier", "SpssDuplicateEntity", identifier, i,
                entityVariable.getName());
          }

          entitiesBuilder.add(new SpssVariableEntity(entityType, identifier, i));
          entityIdentifiers.add(identifier);
        }

        variableEntities = entitiesBuilder.build();

      }

      return variableEntities;
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
        throw new DatasourceParsingException(e.getMessage(), "SpssFailedToLoadData", spssFile.file.getName());
      }
    }

  }

}
