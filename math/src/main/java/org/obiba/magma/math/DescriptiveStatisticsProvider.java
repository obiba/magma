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

  /**
   * Compute the statistical summary of the specified {@code VariableValueSource} over the specified set of {@code
   * VariableEntity}s.
   * <p/>
   * Note that the {@code VariableValueSource} instance should be able to provide a {@code VectorSource} instance.
   * Otherwise, an empty summary is returned (actual values of the summary are unspecified).
   *
   * @param valueSource the {@code VariableValueSource} to evaluate
   * @param entities the set of entities to evaluate over
   * @return a statistical summary (min, max, mean, std dev, etc.)
   */
  public DescriptiveStatistics compute(VariableValueSource valueSource, SortedSet<VariableEntity> entities);

}
