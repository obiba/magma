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

import java.util.SortedSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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
  DescriptiveStatistics compute(VariableValueSource valueSource, SortedSet<VariableEntity> entities);

}
