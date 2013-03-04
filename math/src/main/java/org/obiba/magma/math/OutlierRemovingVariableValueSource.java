package org.obiba.magma.math;

import java.util.SortedSet;

import javax.annotation.Nonnull;

import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.StatisticalSummaryValues;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

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
public class OutlierRemovingVariableValueSource implements VariableValueSource, VectorSource {

  private final ValueTable valueTable;

  private final VariableValueSource wrappedSource;

  private final DescriptiveStatisticsProvider statisticsProvider;

  private transient StatisticalSummary variableStatistics;

  public OutlierRemovingVariableValueSource(ValueTable valueTable, VariableValueSource wrappedSource) {
    this(valueTable, wrappedSource, new ExcludeMissingDescriptiveStatisticsProvider());
  }

  public OutlierRemovingVariableValueSource(ValueTable valueTable, VariableValueSource wrappedSource,
      DescriptiveStatisticsProvider statisticsProvider) {
    if(statisticsProvider == null) throw new IllegalArgumentException("statisticsProvider cannot be null");
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    if(wrappedSource == null) throw new IllegalArgumentException("wrappedSource cannot be null");
    if(wrappedSource.asVectorSource() == null)
      throw new IllegalArgumentException("wrappedSource cannot provide vectors");
    this.statisticsProvider = statisticsProvider;
    this.wrappedSource = wrappedSource;
    this.valueTable = valueTable;
  }

  @Override
  public Variable getVariable() {
    return wrappedSource.getVariable();
  }

  @Nonnull
  @Override
  public Value getValue(ValueSet valueSet) {
    Value value = wrappedSource.getValue(valueSet);
    return isOutlier(value) ? valueForOutlier(value) : value;
  }

  @Override
  public VectorSource asVectorSource() {
    return this;
  }

  @Override
  public ValueType getValueType() {
    return wrappedSource.getValueType();
  }

  @Override
  public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
    return Iterables.transform(wrappedSource.asVectorSource().getValues(entities), new Function<Value, Value>() {

      @Override
      public Value apply(Value from) {
        return isOutlier(from) ? valueForOutlier(from) : from;
      }

    });
  }

  public VariableValueSource getWrappedSource() {
    return wrappedSource;
  }

  /**
   * Determines if {@code value} is an outlier and returns true when it is, false otherwise
   *
   * @param value
   * @return
   */
  protected boolean isOutlier(Value value) {
    if(value.isNull()) {
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

    return value < (mean - sd) || value > (mean + sd);
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
    if(variableStatistics == null) {
      StatisticalSummary summary = this.statisticsProvider
          .compute(getWrappedSource(), Sets.newTreeSet(valueTable.getVariableEntities()));
      // Copy into value-object so we don't keep a reference to the actual values (DescriptiveStatistics keeps all
      // values)
      this.variableStatistics = new StatisticalSummaryValues(summary.getMean(), summary.getVariance(), summary.getN(),
          summary.getMax(), summary.getMin(), summary.getSum());
    }
    return variableStatistics;
  }
}
