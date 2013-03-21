package org.obiba.magma.math;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
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
