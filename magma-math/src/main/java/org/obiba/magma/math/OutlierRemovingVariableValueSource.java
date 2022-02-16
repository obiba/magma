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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.obiba.magma.*;
import org.obiba.magma.support.AbstractVariableValueSourceWrapper;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Detects and removes outliers from a {@code VariableValueSource}. Outliers are "removed" by assigning them another
 * value (usually null).
 * <p/>
 * By default, the definition of an outlier is any value beyond 3 times the standard deviation from the mean. Formally:
 * <p/>
 * <pre>
 * isOutlier = value &lt; (mean - 3 * sd) || value &gt; (mean + 3 * sd);
 * </pre>
 * <p/>
 * When an outlier is detected, its value is replaced by another. By default, a null Value will replace the outlier
 * value.
 *
 * @see OutlierRemovingView
 */
public class OutlierRemovingVariableValueSource extends AbstractVariableValueSourceWrapper implements VectorSource {

  @NotNull
  private final ValueTable valueTable;

  @NotNull
  private final DescriptiveStatisticsProvider statisticsProvider;

  private transient StatisticalSummary variableStatistics;

  public OutlierRemovingVariableValueSource(@NotNull ValueTable valueTable,
                                            @NotNull VariableValueSource wrappedSource) {
    this(valueTable, wrappedSource, new ExcludeMissingDescriptiveStatisticsProvider());
  }

  @SuppressWarnings("ConstantConditions")
  public OutlierRemovingVariableValueSource(@NotNull ValueTable valueTable, @NotNull VariableValueSource wrappedSource,
                                            @NotNull DescriptiveStatisticsProvider statisticsProvider) {
    super(wrappedSource);
    if (statisticsProvider == null) throw new IllegalArgumentException("statisticsProvider cannot be null");
    if (valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    this.statisticsProvider = statisticsProvider;
    this.valueTable = valueTable;
  }

  @NotNull
  @Override
  public Value getValue(ValueSet valueSet) {
    Value value = getWrapped().getValue(valueSet);
    return isOutlier(value) ? valueForOutlier(value) : value;
  }

  @NotNull
  @Override
  public VectorSource asVectorSource() {
    return this;
  }

  @Override
  public Iterable<Value> getValues(Iterable<VariableEntity> entities) {
    return Iterables.transform(getWrapped().asVectorSource().getValues(entities), new Function<Value, Value>() {

      @Override
      public Value apply(Value from) {
        return isOutlier(from) ? valueForOutlier(from) : from;
      }

    });
  }

  /**
   * Determines if {@code value} is an outlier and returns true when it is, false otherwise
   *
   * @param value
   * @return
   */
  protected boolean isOutlier(Value value) {
    if (value.isNull()) {
      return false;
    }
    Number number = (Number) value.getValue();
    return isOutlier(number.doubleValue(), calculateStats());
  }

  /**
   * Determines if the value is an outlier in the context of the computed stats for the variable
   *
   * @param value the value to test
   * @param stats the descriptive statistics of the vector
   * @return true if the value is considered an outlier, false otherwise
   */
  protected boolean isOutlier(double value, StatisticalSummary stats) {
    // If the value lies outside of [mean-3*stdDev,mean+3*stdDev], it is considered an outlier
    double mean = stats.getMean();
    double sd = stats.getStandardDeviation() * 3;

    return value < mean - sd || value > mean + sd;
  }

  /**
   * The value to return for outliers. By default, a null Value is returned.
   *
   * @return
   */
  protected Value valueForOutlier(Value value) {
    return getValueType().nullValue();
  }

  private synchronized StatisticalSummary calculateStats() {
    if (variableStatistics == null) {
      StatisticalSummary summary = statisticsProvider
          .compute(getWrapped(), valueTable.getVariableEntities());
      // Copy into value-object so we don't keep a reference to the actual values (DescriptiveStatistics keeps all
      // values)
      variableStatistics = new StatisticalSummaryValues(summary.getMean(), summary.getVariance(), summary.getN(),
          summary.getMax(), summary.getMin(), summary.getSum());
    }
    return variableStatistics;
  }
}
