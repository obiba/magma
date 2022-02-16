/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math.summary.support;

import com.google.common.collect.Lists;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.obiba.magma.math.ContinuousSummary;
import org.obiba.magma.math.IntervalFrequency;
import org.obiba.magma.math.Distribution;

import java.util.Collection;
import java.util.List;

public class DefaultContinuousSummary extends DefaultFrequenciesSummary implements ContinuousSummary {

  private final DescriptiveStatistics descriptiveStatistics;

  private final List<Double> percentiles = Lists.newArrayList();

  private final Collection<Double> distributionPercentiles = Lists.newArrayList();

  private final List<IntervalFrequency.Interval> intervalFrequencies = Lists.newArrayList();

  public DefaultContinuousSummary(DescriptiveStatistics descriptiveStatistics) {
    this.descriptiveStatistics = descriptiveStatistics;
  }

  @Override
  public long getN() {
    return descriptiveStatistics.getN();
  }

  @Override
  public double getMin() {
    return descriptiveStatistics.getMin();
  }

  @Override
  public double getMax() {
    return descriptiveStatistics.getMax();
  }

  @Override
  public double getSum() {
    return descriptiveStatistics.getSum();
  }

  @Override
  public double getSumsq() {
    return descriptiveStatistics.getSumsq();
  }

  @Override
  public double getMean() {
    return descriptiveStatistics.getMean();
  }

  @Override
  public double getMedian() {
    double median = descriptiveStatistics.apply(new Median());
    return median;
  }

  @Override
  public double getGeometricMean() {
    return descriptiveStatistics.getGeometricMean();
  }

  @Override
  public double getVariance() {
    return descriptiveStatistics.getVariance();
  }

  @Override
  public double getStandardDeviation() {
    return descriptiveStatistics.getStandardDeviation();
  }

  @Override
  public double getSkewness() {
    return descriptiveStatistics.getSkewness();
  }

  @Override
  public double getKurtosis() {
    return descriptiveStatistics.getKurtosis();
  }

  @Override
  public Iterable<Double> getPercentiles() {
    return percentiles;
  }

  public void addPercentile(Double value) {
    percentiles.add(value);
  }

  @Override
  public Iterable<Double> getDistributionPercentiles() {
    return distributionPercentiles;
  }

  public void addDistributionPercentile(Double value) {
    distributionPercentiles.add(value);
  }

  public void computeDistributionPercentiles(Distribution distribution, List<Double> defaultPercentiles) {
    RealDistribution realDistribution = getDistribution(distribution);
    for (Double p : defaultPercentiles) {
      addPercentile(descriptiveStatistics.getPercentile(p));
      if (realDistribution != null) {
        addDistributionPercentile(realDistribution.inverseCumulativeProbability(p / 100d));
      }
    }
  }

  @Override
  public Iterable<IntervalFrequency.Interval> getIntervalFrequencies() {
    return intervalFrequencies;
  }

  public void addIntervalFrequency(IntervalFrequency.Interval interval) {
    intervalFrequencies.add(interval);
  }

  public void computeIntervalFrequencies(int intervals, boolean roundToIntegers) {
    IntervalFrequency intervalFrequency = new IntervalFrequency(getMin(), getMax(), intervals, roundToIntegers);
    for (double d : descriptiveStatistics.getSortedValues()) {
      intervalFrequency.add(d);
    }
    for (IntervalFrequency.Interval interval : intervalFrequency.intervals()) {
      addIntervalFrequency(interval);
    }
  }

  private RealDistribution getDistribution(Distribution distribution) {
    if (distribution.equals(Distribution.normal)) {
      double stddev = descriptiveStatistics.getStandardDeviation();
      return stddev > 0 ? new NormalDistribution(descriptiveStatistics.getMean(), stddev) : null;
    } else {
      return new ExponentialDistribution(descriptiveStatistics.getMean());
    }
  }
}
