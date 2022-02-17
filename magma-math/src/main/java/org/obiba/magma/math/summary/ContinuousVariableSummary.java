/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.math.summary;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.obiba.magma.*;
import org.obiba.magma.math.ContinuousSummary;
import org.obiba.magma.math.Distribution;
import org.obiba.magma.math.Frequency;
import org.obiba.magma.math.IntervalFrequency;
import org.obiba.magma.math.summary.support.DefaultContinuousSummary;
import org.obiba.magma.math.summary.support.DefaultFrequency;
import org.obiba.magma.type.IntegerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 *
 */
public class ContinuousVariableSummary extends AbstractVariableSummary implements ContinuousSummary, Serializable {

  private static final long serialVersionUID = -8679001175321206239L;

  private static final Logger log = LoggerFactory.getLogger(ContinuousVariableSummary.class);


  private final org.apache.commons.math3.stat.Frequency frequencyDist = new org.apache.commons.math3.stat.Frequency();

  private ContinuousSummary continuousSummary;

  private final Collection<Frequency> frequencies = new ArrayList<>();

  @NotNull
  private final Distribution distribution;

  @NotNull
  private List<Double> defaultPercentiles = DEFAULT_PERCENTILES;

  private int intervals = DEFAULT_INTERVALS;

  // Holds missing categories
  // (the case of continuous variables that have "special" values such as 8888 or 9999 that indicate a missing value)
  @NotNull
  private final Set<Value> missing = Sets.newHashSet();

  @NotNull
  private final DescriptiveStatistics descriptiveStats = new DescriptiveStatistics();

  @Override
  public String getCacheKey(ValueTable table) {
    return ContinuousVariableSummaryFactory
        .getCacheKey(variable, table, distribution, defaultPercentiles, intervals, getOffset(), getLimit());
  }

  private ContinuousVariableSummary(@NotNull Variable variable, @NotNull Distribution distribution) {
    super(variable);
    //noinspection ConstantConditions
    Preconditions.checkArgument(distribution != null, "distribution cannot be null");
    Preconditions.checkArgument(variable.getValueType().isNumeric(), "Continuous variables must be numeric");

    this.distribution = distribution;

    if (variable.hasCategories()) {
      for (Category c : variable.getCategories()) {
        if (c.isMissing()) {
          try {
            missing.add(variable.getValueType().valueOf(c.getName()));
          } catch (MagmaRuntimeException e) {
            // When valueOf expects a integer but get a string category, do not crash
            log.warn("Variable {}, Category {}, ValueType ({}): {}", variable.getName(), c.getName(),
                variable.getValueType().getName(), e.getMessage());
          }
        }
      }
    }
  }

  @Override
  public long getN() {
    return continuousSummary.getN();
  }

  @Override
  public double getMin() {
    return continuousSummary.getMin();
  }

  @Override
  public double getMax() {
    return continuousSummary.getMax();
  }

  @Override
  public double getSum() {
    return continuousSummary.getSum();
  }

  @Override
  public double getSumsq() {
    return continuousSummary.getSumsq();
  }

  @Override
  public double getMean() {
    return continuousSummary.getMean();
  }

  @Override
  public double getMedian() {
    return continuousSummary.getMedian();
  }

  @Override
  public double getGeometricMean() {
    return continuousSummary.getGeometricMean();
  }

  @Override
  public double getVariance() {
    return continuousSummary.getVariance();
  }

  @Override
  public double getStandardDeviation() {
    return continuousSummary.getStandardDeviation();
  }

  @Override
  public double getSkewness() {
    return continuousSummary.getSkewness();
  }

  @Override
  public double getKurtosis() {
    return continuousSummary.getKurtosis();
  }

  @Override
  @NotNull
  public Iterable<Double> getPercentiles() {
    return continuousSummary.getPercentiles();
  }

  @Override
  @NotNull
  public Iterable<Double> getDistributionPercentiles() {
    return continuousSummary.getDistributionPercentiles();
  }

  @Override
  @NotNull
  public Iterable<IntervalFrequency.Interval> getIntervalFrequencies() {
    return continuousSummary.getIntervalFrequencies();
  }

  @Override
  @NotNull
  public Iterable<Frequency> getFrequencies() {
    return continuousSummary.getFrequencies();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder implements VariableSummaryBuilder<ContinuousVariableSummary, Builder> {

    private final ContinuousVariableSummary summary;

    @NotNull
    private final Variable variable;

    private boolean addedTable;

    private boolean addedValue;

    public Builder(@NotNull Variable variable, @NotNull Distribution distribution) {
      this.variable = variable;
      summary = new ContinuousVariableSummary(variable, distribution);
    }

    public Builder intervals(int intervals) {
      summary.intervals = intervals;
      return this;
    }

    public Builder defaultPercentiles(@Nullable List<Double> defaultPercentiles) {
      summary.defaultPercentiles = defaultPercentiles == null || defaultPercentiles.isEmpty()
          ? DEFAULT_PERCENTILES
          : defaultPercentiles;
      return this;
    }

    public Builder filter(Integer offset, Integer limit) {
      summary.setOffset(offset);
      summary.setLimit(limit);
      return this;
    }

    @Override
    public Builder addValue(@NotNull Value value) {
      if (addedTable) {
        throw new IllegalStateException("Cannot add value for variable " + summary.getVariable().getName() +
            " because values where previously added from the whole table with addTable().");
      }
      add(value);
      addedValue = true;
      return this;
    }

    @Override
    public Builder addTable(@NotNull ValueTable table, @NotNull ValueSource variableValueSource) {
      if (addedValue) {
        throw new IllegalStateException("Cannot add table for variable " + summary.getVariable().getName() +
            " because values where previously added with addValue().");
      }
      add(table, variableValueSource);
      addedTable = true;

      return this;
    }

    private void add(@NotNull ValueTable table, @NotNull ValueSource variableValueSource) {
      //noinspection ConstantConditions
      Preconditions.checkArgument(table != null, "table cannot be null");
      //noinspection ConstantConditions
      Preconditions.checkArgument(variableValueSource != null, "variableValueSource cannot be null");

      if (!variableValueSource.supportVectorSource()) return;
      VectorSource vs = variableValueSource.asVectorSource();
      Iterable<VariableEntity> entities = summary.getFilteredVariableEntities(table);
      if (vs.supportVectorSummary()) {
        summary.continuousSummary = vs.getVectorSummarySource(entities)
            .asContinuousSummary(summary.distribution, summary.defaultPercentiles, summary.intervals, variable.getCategories());
      }
      // if no pre-computed summary, go through values
      if (summary.continuousSummary == null) {
        for (Value value : vs.getValues(entities)) {
          add(value);
        }
      }
    }

    private void add(@NotNull Value value) {
      //noinspection ConstantConditions
      Preconditions.checkArgument(value != null, "value cannot be null");
      if (value.isNull()) {
        summary.frequencyDist.addValue(NULL_NAME);
      } else {
        if (value.isSequence()) {
          for (Value v : value.asSequence().getValue()) {
            add(v);
          }
        } else {
          if (!summary.missing.contains(value)) {
            summary.descriptiveStats.addValue(((Number) value.getValue()).doubleValue());
          }

          // A continuous variable can have missing categories
          if (value.isNull()) {
            summary.frequencyDist.addValue(NULL_NAME);
          } else if (summary.missing.contains(value)) {
            summary.frequencyDist.addValue(value.toString());
          } else {
            summary.frequencyDist.addValue(NOT_NULL_NAME);
          }
        }
      }
    }

    /**
     * Returns an iterator of frequencyDist names
     */
    private Iterator<String> freqNames(org.apache.commons.math3.stat.Frequency freq) {
      return Iterators.transform(freq.valuesIterator(), new Function<Comparable<?>, String>() {

        @Override
        public String apply(Comparable<?> input) {
          return input.toString();
        }
      });
    }

    @SuppressWarnings("MagicNumber")
    private void compute() {
      log.trace("Start compute continuous {}", summary.variable.getName());
      if (summary.continuousSummary == null) {
        DefaultContinuousSummary continuousSummary = new DefaultContinuousSummary(summary.descriptiveStats);
        // frequencies
        Iterator<String> concat = freqNames(summary.frequencyDist);
        // Iterate over all values (N/A and NOT_EMPTY)
        while (concat.hasNext()) {
          String value = concat.next();
          continuousSummary.addFrequency(new DefaultFrequency(value, summary.frequencyDist.getCount(value),
              Double.isNaN(summary.frequencyDist.getPct(value)) ? 0.0 : summary.frequencyDist.getPct(value),
              !NOT_NULL_NAME.equals(value)));
        }

        double variance = continuousSummary.getVariance();
        if (!(Double.isNaN(variance) || Double.isInfinite(variance) || variance <= 0)) {
          // interval frequencies
          continuousSummary.computeIntervalFrequencies(summary.intervals, summary.getVariable().getValueType() == IntegerType.get());
          // percentiles
          continuousSummary.computeDistributionPercentiles(summary.distribution, summary.defaultPercentiles);
        }
        summary.continuousSummary = continuousSummary;
      }
    }

    @NotNull
    @Override
    public Variable getVariable() {
      return variable;
    }

    @Override
    @NotNull
    public ContinuousVariableSummary build() {
      compute();
      return summary;
    }
  }

}