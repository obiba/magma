package org.obiba.magma.math;

import java.util.SortedSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.obiba.magma.Value;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

/**
 * Abstract implementation of {@code DescriptiveStatisticsProvider} extending classes should implement {@code
 * #processValue(VariableValueSource, Value, DescriptiveStatistics)} by either adding the value to the instance of
 * {@code DescriptiveStatistics} or not
 */
public abstract class AbstractDescriptiveStatisticsProvider implements DescriptiveStatisticsProvider {

  @Override
  public DescriptiveStatistics compute(VariableValueSource valueSource, SortedSet<VariableEntity> entities) {
    if(valueSource == null) throw new IllegalArgumentException("valueSource cannot be null");
    if(entities == null) throw new IllegalArgumentException("entities cannot be null");

    DescriptiveStatistics ds = new DescriptiveStatistics();
    if(valueSource.supportVectorSource()) {
      for(Value value : valueSource.asVectorSource().getValues(entities)) {
        processValue(valueSource, value, ds);
      }
    }
    return ds;
  }

  protected abstract void processValue(VariableValueSource valueSource, Value value, DescriptiveStatistics stats);

}
