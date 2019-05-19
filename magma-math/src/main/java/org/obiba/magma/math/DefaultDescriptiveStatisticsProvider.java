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
import org.obiba.magma.VariableValueSource;

/**
 * A default implementation of {@code DescriptiveStatisticsProvider} that will exclude all null values from the
 * statistical summary. All other values are considered.
 */
public class DefaultDescriptiveStatisticsProvider extends AbstractDescriptiveStatisticsProvider {

  @Override
  protected void processValue(VariableValueSource valueSource, Value value, DescriptiveStatistics stats) {
    if(!value.isNull()) {
      stats.addValue(((Number) value.getValue()).doubleValue());
    }
  }

}
