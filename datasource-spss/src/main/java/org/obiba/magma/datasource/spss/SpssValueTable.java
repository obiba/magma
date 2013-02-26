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
import java.io.File;
import java.util.Date;
import java.util.Set;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.spss.support.SpssVariableValueSourceFactory;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.DateTimeType;
import org.opendatafoundation.data.spss.SPSSFileException;

import com.google.common.collect.Sets;

public class SpssValueTable extends AbstractValueTable {

  private final File spssFile;

  public SpssValueTable(SpssDatasource datasource, String name, String entityType, File spssFile) {
    super(datasource, name);
    this.spssFile = spssFile;
    setVariableEntityProvider(new SpssVariableEntityProvider(entityType));
  }

  @Override
  public void initialise() {
    try {
      initializeVariableSources();
    } catch(IOException e) {
      throw new MagmaRuntimeException(e);
    } catch(SPSSFileException e) {
      throw new MagmaRuntimeException(e);
    }
    super.initialise();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    throw new NoSuchValueSetException(this, entity);
  }

  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {
      @Override
      public Value getLastUpdate() {
        Date lastModified = new Date(spssFile.lastModified());
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

  private void initializeVariableSources() throws IOException, SPSSFileException {
    addVariableValueSources(new SpssVariableValueSourceFactory(spssFile));
  }

  //
  // Inner Classes
  //

  private class SpssVariableEntityProvider implements VariableEntityProvider {

    private final String entityType;

    private SpssVariableEntityProvider(String entityType) {
      this.entityType = entityType == null || entityType.trim().isEmpty() ? "Participant" : entityType.trim();
    }

    @Override
    public String getEntityType() {
      return entityType;
    }

    @Override
    public boolean isForEntityType(String entityType) {
      return getEntityType().equals(entityType);
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return Sets.newHashSet();
    }
  }

}
