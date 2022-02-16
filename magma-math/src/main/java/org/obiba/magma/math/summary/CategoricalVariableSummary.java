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
import com.google.common.collect.*;
import org.obiba.magma.*;
import org.obiba.magma.math.CategoricalSummary;
import org.obiba.magma.math.Frequency;
import org.obiba.magma.math.summary.support.DefaultCategoricalSummary;
import org.obiba.magma.math.summary.support.DefaultFrequency;
import org.obiba.magma.type.BooleanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CategoricalVariableSummary extends AbstractVariableSummary implements CategoricalSummary, Serializable {

  private static final long serialVersionUID = 203198842420473154L;

  private static final Logger log = LoggerFactory.getLogger(CategoricalVariableSummary.class);

  private final org.apache.commons.math3.stat.Frequency frequencyDist = new org.apache.commons.math3.stat.Frequency();

  private CategoricalSummary categoricalSummary;

  private boolean distinct;

  private CategoricalVariableSummary(@NotNull Variable variable) {
    super(variable);
  }

  @Override
  public String getCacheKey(ValueTable table) {
    return CategoricalVariableSummaryFactory.getCacheKey(variable, table, distinct, getOffset(), getLimit());
  }

  @Override
  @NotNull
  public Iterable<Frequency> getFrequencies() {
    return categoricalSummary.getFrequencies();
  }

  @Override
  public String getMode() {
    return categoricalSummary.getMode();
  }

  @Override
  public long getN() {
    return categoricalSummary.getN();
  }

  @Override
  public long getOtherFrequency() {
    return categoricalSummary.getOtherFrequency();
  }

  public boolean isDistinct() {
    return distinct;
  }

  public void setDistinct(boolean distinct) {
    this.distinct = distinct;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder implements VariableSummaryBuilder<CategoricalVariableSummary, Builder> {

    private final CategoricalVariableSummary summary;

    @NotNull
    private final Variable variable;

    private boolean addedTable;

    private boolean addedValue;

    public Builder(@NotNull Variable variable) {
      this.variable = variable;
      summary = new CategoricalVariableSummary(variable);
    }

    @Override
    public Builder addValue(@NotNull Value value) {
      if (addedTable) {
        throw new IllegalStateException("Cannot add value for variable " + summary.variable.getName() +
            " because values where previously added from the whole table with addTable().");
      }
      add(value, categoryNames());
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
        summary.categoricalSummary = vs.getVectorSummarySource(entities).asCategoricalSummary(variable.getCategories());
      }
      // if no pre-computed summary, go through values
      if (summary.categoricalSummary == null) {
        for (Value value : vs.getValues(entities)) {
          add(value, categoryNames());
        }
      }
    }

    private void add(@NotNull Value value, List<String> categoryNames) {
      //noinspection ConstantConditions
      Preconditions.checkArgument(value != null, "value cannot be null");

      if (value.isSequence()) {
        if (value.isNull()) {
          summary.frequencyDist.addValue(CategoricalSummary.NULL_NAME);
        } else {
          for (Value v : value.asSequence().getValue()) {
            add(v, categoryNames);
          }
        }
      } else {
        if (value.isNull()) {
          summary.frequencyDist.addValue(CategoricalSummary.NULL_NAME);
        } else {
          String valueStr = value.toString();
          String intValueStr = null;
          if (value.getValueType().isNumeric() && valueStr.endsWith(".0"))
            intValueStr = valueStr.substring(0, valueStr.length() - 2);
          if (summary.distinct || categoryNames.contains(valueStr)) {
            summary.frequencyDist.addValue(valueStr);
          } else if (intValueStr != null && categoryNames.contains(intValueStr)) {
            summary.frequencyDist.addValue(intValueStr);
          } else {
            summary.frequencyDist.addValue(CategoricalSummary.OTHER_NAME);
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

    /**
     * Returns an iterator of category names
     */
    private List<String> categoryNames() {
      if (variable.getValueType().equals(BooleanType.get())) {
        return ImmutableList.<String>builder() //
            .add(BooleanType.get().trueValue().toString()) //
            .add(BooleanType.get().falseValue().toString()).build();
      }

      return Lists.newArrayList(Iterables.transform(variable.getCategories(), new Function<Category, String>() {

        @Override
        public String apply(Category from) {
          return from.getName();
        }

      }));
    }

    private Map<String, Category> getCategoriesByName() {
      return Maps.uniqueIndex(variable.getCategories(), new Function<Category, String>() {
        @Override
        public String apply(Category input) {
          return input.getName();
        }
      });
    }

    private void compute() {
      log.trace("Start compute categorical {}", summary.variable.getName());
      if (summary.categoricalSummary == null) {
        DefaultCategoricalSummary defaultCategoricalSummary = new DefaultCategoricalSummary();

        long max = 0;
        Iterator<String> concat = summary.distinct //
            ? freqNames(summary.frequencyDist)  // category names, null values and distinct values
            : Iterators.concat(categoryNames().iterator(),
            ImmutableList.of(CategoricalSummary.NULL_NAME).iterator()); // category names and null values

        // Iterate over all category names including or not distinct values.
        // The loop will also determine the mode of the distribution (most frequent value)
        Map<String, Category> categoriesByName = getCategoriesByName();
        while (concat.hasNext()) {
          String value = concat.next();
          long count = summary.frequencyDist.getCount(value);
          if (count > max) {
            max = count;
            defaultCategoricalSummary.setMode(value);
          }

          boolean notMissing = variable.getValueType().equals(BooleanType.get())
              ? value.equals(BooleanType.get().trueValue().toString()) ||
              value.equals(BooleanType.get().falseValue().toString())
              : categoriesByName.containsKey(value) && !categoriesByName.get(value).isMissing();

          defaultCategoricalSummary.addFrequency(new DefaultFrequency(value, summary.frequencyDist.getCount(value),
              Double.isNaN(summary.frequencyDist.getPct(value)) ? 0.0 : summary.frequencyDist.getPct(value),
              !notMissing));
        }
        defaultCategoricalSummary.setOtherFrequency(summary.frequencyDist.getCount(CategoricalSummary.OTHER_NAME));
        defaultCategoricalSummary.setN(summary.frequencyDist.getSumFreq());

        summary.categoricalSummary = defaultCategoricalSummary;
      }
    }

    public Builder distinct(boolean distinct) {
      summary.setDistinct(distinct);
      return this;
    }

    public Builder filter(Integer offset, Integer limit) {
      summary.setOffset(offset);
      summary.setLimit(limit);
      return this;
    }

    @Override
    @NotNull
    public CategoricalVariableSummary build() {
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