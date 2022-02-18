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
import org.obiba.magma.math.ContinuousSummary;
import org.obiba.magma.math.Interval;

import java.util.List;

public class DefaultContinuousSummary extends DefaultFrequenciesSummary implements ContinuousSummary {

  private final List<Interval> intervalFrequencies = Lists.newArrayList();
  private final List<Double> percentiles = Lists.newArrayList();
  private final List<Double> distributionPercentiles = Lists.newArrayList();

  private double min = 0;
  private double max = 0;
  private double sum = 0;
  private double sumsq = 0;
  private double mean = 0;
  private double median = 0;
  private double geomean = 0;
  private double variance = 0;
  private double stddev = 0;
  private double skewness = 0;
  private double kurtosis = 0;

  @Override
  public double getMin() {
    return min;
  }

  public void setMin(double min) {
    this.min = min;
  }

  @Override
  public double getMax() {
    return max;
  }

  public void setMax(double max) {
    this.max = max;
  }

  @Override
  public double getSum() {
    return sum;
  }

  public void setSum(double sum) {
    this.sum = sum;
  }

  @Override
  public double getSumsq() {
    return sumsq;
  }

  public void setSumsq(double sumsq) {
    this.sumsq = sumsq;
  }

  @Override
  public double getMean() {
    return mean;
  }

  public void setMean(double mean) {
    this.mean = mean;
  }

  @Override
  public double getMedian() {
    return median;
  }

  public void setMedian(double median) {
    this.median = median;
  }

  @Override
  public double getGeometricMean() {
    return geomean;
  }

  public void setGeometricMean(double geomean) {
    this.geomean = geomean;
  }

  @Override
  public double getVariance() {
    return variance;
  }

  public void setVariance(double variance) {
    this.variance = variance;
  }

  @Override
  public double getStandardDeviation() {
    return stddev;
  }

  public void setStandardDeviation(double stddev) {
    this.stddev = stddev;
  }

  @Override
  public double getSkewness() {
    return skewness;
  }

  public void setSkewness(double skewness) {
    this.skewness = skewness;
  }

  @Override
  public double getKurtosis() {
    return kurtosis;
  }

  public void setKurtosis(double kurtosis) {
    this.kurtosis = kurtosis;
  }

  @Override
  public Iterable<Double> getPercentiles() {
    return percentiles;
  }

  public void addPercentile(double value) {
    percentiles.add(value);
  }

  @Override
  public Iterable<Double> getDistributionPercentiles() {
    return distributionPercentiles;
  }

  public void addDistributionPercentile(double value) {
    distributionPercentiles.add(value);
  }

  @Override
  public Iterable<Interval> getIntervalFrequencies() {
    return intervalFrequencies;
  }

  public void addIntervalFrequency(Interval interval) {
    intervalFrequencies.add(interval);
  }

}
