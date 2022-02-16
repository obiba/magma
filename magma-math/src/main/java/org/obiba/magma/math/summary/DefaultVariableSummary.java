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
import com.google.common.collect.Iterators;
import org.obiba.magma.*;
import org.obiba.magma.math.FrequenciesSummary;
import org.obiba.magma.math.Frequency;
import org.obiba.magma.math.summary.support.DefaultFrequenciesSummary;
import org.obiba.magma.math.summary.support.DefaultFrequency;
import org.obiba.magma.type.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class DefaultVariableSummary extends AbstractVariableSummary implements FrequenciesSummary, Serializable {

  private static final long serialVersionUID = 203198842420473154L;

  private static final Logger log = LoggerFactory.getLogger(DefaultVariableSummary.class);

  private static final SimpleDateFormat ISO_8601_NO_TZ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public static final String NOT_NULL_NAME = "NOT_NULL";

  private final org.apache.commons.math3.stat.Frequency frequencyDist = new org.apache.commons.math3.stat.Frequency();

  private FrequenciesSummary frequenciesSummary;

  private DefaultVariableSummary(@NotNull Variable variable) {
    super(variable);
  }

  @Override
  public String getCacheKey(ValueTable table) {
    return DefaultVariableSummaryFactory.getCacheKey(variable, table, getOffset(), getLimit());
  }

  @Override
  @NotNull
  public Iterable<Frequency> getFrequencies() {
    return frequenciesSummary.getFrequencies();
  }

  @Override
  public long getN() {
    return frequenciesSummary.getN();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder implements VariableSummaryBuilder<DefaultVariableSummary, Builder> {

    private final DefaultVariableSummary summary;

    @NotNull
    private final Variable variable;

    private final List<String> missings;

    private boolean addedTable;

    private boolean addedValue;

    public Builder(@NotNull Variable variable) {
      this.variable = variable;
      this.missings = variable.getCategories().stream()
          .filter(Category::isMissing)
          .map(Category::getName)
          .collect(Collectors.toList());
      summary = new DefaultVariableSummary(variable);
    }

    @Override
    public Builder addValue(@NotNull Value value) {
      if (addedTable) {
        throw new IllegalStateException("Cannot add value for variable " + summary.variable.getName() +
            " because values where previously added from the whole table with addTable().");
      }
      add(value);
      addedValue = true;
      return this;
    }

    @Override
    public Builder addTable(@NotNull ValueTable table, @NotNull ValueSource valueSource) {
      if (addedValue) {
        throw new IllegalStateException("Cannot add table for variable " + summary.variable.getName() +
            " because values where previously added with addValue().");
      }
      add(table, valueSource);
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
        summary.frequenciesSummary = vs.getVectorSummarySource(entities).asFrequenciesSummary();
      }
      // if no pre-computed summary, go through values
      if (summary.frequenciesSummary == null) {
        for (Value value : vs.getValues(entities)) {
          add(value);
        }
      }
    }

    private void add(@NotNull Value value) {
      //noinspection ConstantConditions
      Preconditions.checkArgument(value != null, "value cannot be null");

      if (value.isSequence()) {
        if (value.isNull()) {
          summary.frequencyDist.addValue(FrequenciesSummary.NULL_NAME);
        } else {
          for (Value v : value.asSequence().getValue()) {
            add(v);
          }
        }
      } else if (value.isNull()) {
        summary.frequencyDist.addValue(FrequenciesSummary.NULL_NAME);
      } else {
        boolean added = false;
        if (!missings.isEmpty()) {
          String valueStr;
          if (DateTimeType.get().equals(variable.getValueType())) {
            valueStr = ISO_8601_NO_TZ.format((Date) value.getValue()); // most likely the category name does not contain the time zone
          } else {
            valueStr = value.toString();
          }
          if (missings.contains(valueStr)) {
            summary.frequencyDist.addValue(valueStr);
            added = true;
          }
        }
        if (!added) {
          summary.frequencyDist.addValue(NOT_NULL_NAME);
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

    private void compute() {
      log.trace("Start compute default summary {}", summary.variable);
      if (summary.frequenciesSummary == null) {
        DefaultFrequenciesSummary frequenciesSummary = new DefaultFrequenciesSummary();
        long max = 0;
        Iterator<String> concat = freqNames(summary.frequencyDist);

        // Iterate over all values.
        // The loop will also determine the mode of the distribution (most frequent value)
        while (concat.hasNext()) {
          String value = concat.next();
          long count = summary.frequencyDist.getCount(value);
          if (count > max) {
            max = count;
          }
          frequenciesSummary.addFrequency(new DefaultFrequency(value, summary.frequencyDist.getCount(value),
              Double.isNaN(summary.frequencyDist.getPct(value)) ? 0.0 : summary.frequencyDist.getPct(value),
              missings.contains(value) || value.equals(FrequenciesSummary.NULL_NAME)));
        }
        frequenciesSummary.sortFrequencies();
        frequenciesSummary.setN(summary.frequencyDist.getSumFreq());

        summary.frequenciesSummary = frequenciesSummary;
      }
    }

    public Builder filter(Integer offset, Integer limit) {
      summary.setOffset(offset);
      summary.setLimit(limit);
      return this;
    }

    @Override
    @NotNull
    public DefaultVariableSummary build() {
      compute();
      return summary;
    }

    @NotNull
    @Override
    public Variable getVariable() {
      return variable;
    }

  }

}