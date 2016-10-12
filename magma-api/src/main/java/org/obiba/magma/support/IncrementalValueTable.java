/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.support;

import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.views.AbstractTransformingValueTableWrapper;

import com.google.common.collect.Maps;

/**
 *
 */
public class IncrementalValueTable extends AbstractTransformingValueTableWrapper {

//  private static final Logger log = LoggerFactory.getLogger(IncrementalValueTable.class);

  private final IncrementalFunction variableEntityMappingFunction;

  @Override
  public ValueTable getWrappedValueTable() {
    return sourceTable;
  }

  public static class Factory {

    private Factory() {
    }

    public static ValueTable create(@NotNull ValueTable sourceTable, @Nullable ValueTable destinationTable) {
      return destinationTable == null ? sourceTable : new IncrementalValueTable(sourceTable, destinationTable);
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

  @NotNull
  private final ValueTable sourceTable;

  @NotNull
  private final ValueTable destinationTable;

  private IncrementalValueTable(@NotNull ValueTable sourceTable, @NotNull ValueTable destinationTable) {
    this.sourceTable = sourceTable;
    this.destinationTable = destinationTable;
    variableEntityMappingFunction = new IncrementalFunction();
  }

  @NotNull
  @Override
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return variableEntityMappingFunction;
  }

  /**
   * <pre>
   * apply: source entity <b>older</b> than destination
   * unapply: source entity <b>newer</b> than destination
   * </pre>
   */
  private class IncrementalFunction implements BijectiveFunction<VariableEntity, VariableEntity> {

    private final Map<VariableEntity, VariableEntity> applyCache = Maps.newHashMap();

    @Override
    public VariableEntity apply(VariableEntity from) {
      if(applyCache.containsKey(from)) {
        //log.info("apply has cached: {}", from.getIdentifier());
        return applyCache.get(from);
      }
      boolean newer = isSourceNewerThanDestination(from);
      VariableEntity entity = newer ? from : null;
      //log.info("View: {}, entity: {}, sourceIsNewer: {}, return {}", getName(), from, newer, entity);
      applyCache.put(from, entity);
      return entity;
    }

    @Override
    public VariableEntity unapply(VariableEntity from) {
      return from;
    }

    private boolean isSourceNewerThanDestination(VariableEntity from) {
      Timestamps sourceTimestamps = null;
      Value sourceLastUpdate = DateTimeType.get().nullValue();
      Value destinationLastUpdate = DateTimeType.get().nullValue();
      try {
        sourceTimestamps = getWrappedValueTable().getValueSetTimestamps(from);
        if(sourceTimestamps != null) sourceLastUpdate = sourceTimestamps.getLastUpdate();
      } catch(NoSuchValueSetException ignored) {
      }

      Timestamps destinationTimestamps = null;
      try {
        destinationTimestamps = destinationTable.getValueSetTimestamps(from);
        if(destinationTimestamps != null) destinationLastUpdate = destinationTimestamps.getLastUpdate();
      } catch(NoSuchValueSetException ignored) {
      }

      //log.info("{} - sourceLastUpdate: {}, destinationLastUpdate: {}", from, sourceLastUpdate, destinationLastUpdate);
      return sourceLastUpdate.isNull() || destinationLastUpdate.isNull() ||
          sourceLastUpdate.compareTo(destinationLastUpdate) > 0;
    }
  }

}
