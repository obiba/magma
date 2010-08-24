package org.obiba.magma.math;

import java.util.SortedSet;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

/**
 * Strategy for computing a statistical summary of a {@code VariableValueSource}. Implementations can choose how to
 * handle certain values (such as values that represent missing data).
 */
public interface DescriptiveStatisticsProvider {

  public DescriptiveStatistics compute(VariableValueSource valueSource, SortedSet<VariableEntity> entities);

}
