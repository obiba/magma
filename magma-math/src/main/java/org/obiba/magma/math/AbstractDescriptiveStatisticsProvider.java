/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.obiba.magma.Value;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

import java.util.List;

/**
 * Abstract implementation of {@code DescriptiveStatisticsProvider} extending classes should implement {@code
 * #processValue(VariableValueSource, Value, DescriptiveStatistics)} by either adding the value to the instance of
 * {@code DescriptiveStatistics} or not
 */
public abstract class AbstractDescriptiveStatisticsProvider implements DescriptiveStatisticsProvider {

  @Override
  public DescriptiveStatistics compute(VariableValueSource valueSource, List<VariableEntity> entities) {
    if (valueSource == null) throw new IllegalArgumentException("valueSource cannot be null");
    if (entities == null) throw new IllegalArgumentException("entities cannot be null");

    DescriptiveStatistics ds = new DescriptiveStatistics();
    if (valueSource.supportVectorSource()) {
      for (Value value : valueSource.asVectorSource().getValues(entities)) {
        processValue(valueSource, value, ds);
      }
    }
    return ds;
  }

  protected abstract void processValue(VariableValueSource valueSource, Value value, DescriptiveStatistics stats);

}
