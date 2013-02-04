/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.support;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("ClassTooDeepInInheritanceTree")
public class IncrementalView extends View {

  private static final Logger log = LoggerFactory.getLogger(IncrementalView.class);

  public static class Factory {

    private Factory() {
    }

    public static ValueTable create(@Nonnull ValueTable sourceTable, @Nullable ValueTable destinationTable) {
      return destinationTable == null ? sourceTable : new IncrementalView(sourceTable, destinationTable);
    }

    public static ValueTable create(ValueTable sourceTable, Datasource destinationDatasource) {
      ValueTable destinationTable = null;
      try {
        destinationTable = destinationDatasource.getValueTable(sourceTable.getName());
      } catch(NoSuchValueTableException ignored) {
      }
      return create(sourceTable, destinationTable);
    }

  }

  @Nonnull
  private final ValueTable destinationTable;

  private IncrementalView(ValueTable sourceTable, @Nonnull ValueTable destinationTable) {
    super(sourceTable.getName(), sourceTable);
    this.destinationTable = destinationTable;
  }

  @Override
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return new IncrementalFunction();
  }

  /**
   * <pre>
   * apply: source entity <b>older</b> than destination
   * unapply: source entity <b>newer</b> than destination
   * </pre>
   */
  private class IncrementalFunction implements BijectiveFunction<VariableEntity, VariableEntity> {

    @Override
    public VariableEntity apply(VariableEntity from) {
      boolean newer = isSourceNewerThanDestination(from);
      VariableEntity entity = newer ? from : null;
      log.debug("View: {}, entity: {}, sourceIsNewer: {}, return {}", getName(), from, newer, entity);
      return entity;
    }

    @Override
    public VariableEntity unapply(VariableEntity from) {
      return from;
    }

    private boolean isSourceNewerThanDestination(VariableEntity from) {
      Timestamps sourceTimestamps = null;
      try {
        sourceTimestamps = getWrappedValueTable().getValueSetTimestamps(from);
      } catch(NoSuchValueSetException ignored) {
      }

      Timestamps destinationTimestamps = null;
      try {
        destinationTimestamps = destinationTable.getValueSetTimestamps(from);
      } catch(NoSuchValueSetException ignored) {
      }

      Value sourceLastUpdate = sourceTimestamps == null || sourceTimestamps.equals(NullTimestamps.get()) //
          ? DateTimeType.get().nullValue() //
          : sourceTimestamps.getLastUpdate();
      Value destinationLastUpdate = destinationTimestamps == null || destinationTimestamps.equals(NullTimestamps.get())
          ? DateTimeType.get().nullValue()
          : destinationTimestamps.getLastUpdate();
      log.trace("{} - sourceLastUpdate: {}, destinationLastUpdate: {}", from, sourceLastUpdate, destinationLastUpdate);
      return sourceLastUpdate.isNull() || destinationLastUpdate.isNull() ||
          sourceLastUpdate.compareTo(destinationLastUpdate) > 0;
    }
  }

}
