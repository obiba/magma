/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.math.summary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

/**
 *
 */
public class TextVariableSummary extends AbstractVariableSummary implements Serializable {

  private static final long serialVersionUID = 203198842420473154L;

  private static final Logger log = LoggerFactory.getLogger(TextVariableSummary.class);

  public static final String NULL_NAME = "N/A";

  public static final String NOT_NULL_NAME = "NOT_NULL";

  private final org.apache.commons.math3.stat.Frequency frequencyDist = new org.apache.commons.math3.stat.Frequency();

  private long n;

  private boolean empty = true;

  private final List<Frequency> frequencies = new ArrayList<>();

  private TextVariableSummary(@NotNull Variable variable) {
    super(variable);
  }

  @Override
  public String getCacheKey(ValueTable table) {
    return TextVariableSummaryFactory.getCacheKey(variable, table, getOffset(), getLimit());
  }

  @NotNull
  public Iterable<Frequency> getFrequencies() {
    return ImmutableList.copyOf(frequencies);
  }

  public long getN() {
    return n;
  }

  public boolean isEmpty() {
    return empty;
  }

  public static class Frequency implements Serializable {

    private static final long serialVersionUID = -2876592652764310324L;

    private final String value;

    private final long freq;

    private final double pct;

    private final boolean missing;

    public Frequency(String value, long freq, double pct, boolean missing) {
      this.value = value;
      this.freq = freq;
      this.pct = pct;
      this.missing = missing;
    }

    public String getValue() {
      return value;
    }

    public long getFreq() {
      return freq;
    }

    public double getPct() {
      return pct;
    }

    public boolean isMissing() {
      return missing;
    }
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder implements VariableSummaryBuilder<TextVariableSummary, Builder> {

    private final TextVariableSummary summary;

    @NotNull
    private final Variable variable;

    private boolean addedTable;

    private boolean addedValue;

    public Builder(@NotNull Variable variable) {
      this.variable = variable;
      summary = new TextVariableSummary(variable);
    }

    @Override
    public Builder addValue(@NotNull Value value) {
      if(addedTable) {
        throw new IllegalStateException("Cannot add value for variable " + summary.variable.getName() +
            " because values where previously added from the whole table with addTable().");
      }
      add(value);
      addedValue = true;
      return this;
    }

    @Override
    public Builder addTable(@NotNull ValueTable table, @NotNull ValueSource valueSource) {
      if(addedValue) {
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

      if(!variableValueSource.supportVectorSource()) return;
      for(Value value : variableValueSource.asVectorSource().getValues(summary.getFilteredVariableEntities(table))) {
        add(value);
      }
    }

    private void add(@NotNull Value value) {
      //noinspection ConstantConditions
      Preconditions.checkArgument(value != null, "value cannot be null");

      if(summary.empty) summary.empty = false;
      if(value.isSequence()) {
        if(value.isNull()) {
          summary.frequencyDist.addValue(NULL_NAME);
        } else {
          for(Value v : value.asSequence().getValue()) {
            add(v);
          }
        }
      } else {
        summary.frequencyDist.addValue(value.isNull() ? NULL_NAME : value.toString());
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
      Iterator<String> concat = freqNames(summary.frequencyDist);

      // Iterate over all category names including or not distinct values.
      // The loop will also determine the mode of the distribution (most frequent value)
      while(concat.hasNext()) {
        String value = concat.next();
        summary.frequencies.add(new Frequency(value, summary.frequencyDist.getCount(value),
            Double.isNaN(summary.frequencyDist.getPct(value)) ? 0.0 : summary.frequencyDist.getPct(value),
            value.equals(NULL_NAME)));
      }

      Collections.sort(summary.frequencies, new Comparator<Frequency>() {
        @Override
        public int compare(Frequency o1, Frequency o2) {
          return (int) (o2.getFreq() - o1.getFreq());
        }
      });

      summary.n = summary.frequencyDist.getSumFreq();
    }

    public Builder filter(Integer offset, Integer limit) {
      summary.setOffset(offset);
      summary.setLimit(limit);
      return this;
    }

    @Override
    @NotNull
    public TextVariableSummary build() {
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