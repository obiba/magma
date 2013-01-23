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

import org.obiba.magma.NoSuchValueSetException;
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
public class IncrementalView extends View {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final ValueTable destinationTable;

  public IncrementalView(ValueTable sourceTable, ValueTable destinationTable) {
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
      return isSourceNewerThanDestination(from) ? null : from;
    }

    @Override
    public VariableEntity unapply(VariableEntity from) {
      return isSourceNewerThanDestination(from) ? from : null;
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

      Value v1 = sourceTimestamps == null || sourceTimestamps.equals(NullTimestamps.get()) //
          ? DateTimeType.get().nullValue() //
          : sourceTimestamps.getLastUpdate();
      Value v2 = destinationTimestamps == null || destinationTimestamps.equals(NullTimestamps.get()) //
          ? DateTimeType.get().nullValue() //
          : destinationTimestamps.getLastUpdate();
      log.debug("source.updated: {}, destination.updated: {}", v1, v2);
      return v1.isNull() || v2.isNull() || v1.compareTo(v2) > 0;
    }
  }

}
