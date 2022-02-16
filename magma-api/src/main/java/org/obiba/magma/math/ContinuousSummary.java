/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU License v3.0.
 *
 * You should have received a copy of the GNU General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math;

import com.google.common.collect.ImmutableList;

import java.util.List;

public interface ContinuousSummary extends FrequenciesSummary {

  int DEFAULT_INTERVALS = 10;

  List<Double> DEFAULT_PERCENTILES = ImmutableList
      .of(0.05d, 0.5d, 5d, 10d, 15d, 20d, 25d, 30d, 35d, 40d, 45d, 50d,
          55d, 60d, 65d, 70d, 75d, 80d, 85d, 90d, 95d, 99.5d, 99.95d);

  double getMin();

  double getMax();

  double getSum();

  double getSumsq();

  double getMean();

  double getMedian();

  double getGeometricMean();

  double getVariance();

  double getStandardDeviation();

  double getSkewness();

  double getKurtosis();

  Iterable<Double> getPercentiles();

  Iterable<Double> getDistributionPercentiles();

  Iterable<IntervalFrequency.Interval> getIntervalFrequencies();

  Iterable<Frequency> getFrequencies();

}
