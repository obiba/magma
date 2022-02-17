/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import org.obiba.magma.math.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface VectorSummarySource {

  /**
   * Basic frequency count summary. If detailed, reports all observations, otherwise only report nulls/non nulls.
   *
   * @param detailed
   * @return
   */
  @Nullable
  FrequenciesSummary asFrequenciesSummary(boolean detailed);

  /**
   * Get the summary statistics for the given categories.
   *
   * @param categories
   * @return
   */
  @Nullable
  CategoricalSummary asCategoricalSummary(Set<Category> categories);

  /**
   * Get the summary statistics of a vector with numerical values. Some categories may
   * describe missing values, which will be excluded from the descriptive statistics and have their
   * frequencies counted.
   *
   * @param distribution
   * @param defaultPercentiles
   * @param intervals
   * @param categories
   * @return
   */
  @Nullable
  ContinuousSummary asContinuousSummary(Distribution distribution, List<Double> defaultPercentiles, int intervals, Set<Category> categories);

  /**
   * Get the summary statistics for geolocalized values.
   *
   * @return
   */
  @Nullable
  GeoSummary asGeoSummary();

}
