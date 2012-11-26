package org.obiba.magma.math;

import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.views.View;

import com.google.common.collect.Maps;

/**
 * A {@code View} that removes outlier values from the underlying table. Note that outliers can only be removed for
 * variables that have a numerical value type ({@code ValueType#isNumeric()} returns true).
 * @see OutlierRemovingVariableValueSource
 */
public class OutlierRemovingView extends View {

  private final WrappingFunction function = new WrappingFunction();

  private final DescriptiveStatisticsProvider statisticsProvider;

  /**
   * A cache of {@code OutlierRemovingVariableValueSource}. This holds an instance of {@code
   * OutlierRemovingVariableValueSource} for each {@code VariableValueSource} in the wrapped table. Note that this cache
   * is lazily constructed.
   */
  private final Map<String, OutlierRemovingVariableValueSource> sources = Maps.newHashMap();

  public OutlierRemovingView() {
    this(new ExcludeMissingDescriptiveStatisticsProvider());
  }

  public OutlierRemovingView(DescriptiveStatisticsProvider statisticsProvider) {
    if(statisticsProvider == null) throw new IllegalArgumentException("statisticsProvider cannot be null");
    this.statisticsProvider = statisticsProvider;
  }

  @Override
  public BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction() {
    return function;
  }

  /**
   * Returns true when the {@code VariableValueSource} is a candidate for removing outliers. This method tests that the
   * source can provide a non-null {@code VectorSource}, that the variable is not repeatable and that its {@code
   * ValueType} is numeric.
   * @param source
   * @return
   */
  protected boolean canRemoveOutliers(@Nullable VariableValueSource source) {
    return source != null && source.asVectorSource() != null //
        && source.getVariable().isRepeatable() == false //
        && source.getValueType().isNumeric();
  }

  /**
   * Lookup a {@code OutlierRemovingVariableValueSource} for the corresponding {@code VariableValueSource}, if an entry
   * does not exist, a new one is created and returned.
   * @param from
   * @return
   */
  protected synchronized OutlierRemovingVariableValueSource cacheLookup(@Nullable VariableValueSource from) {
    String variableName = from == null ? null : from.getVariable().getName();
    OutlierRemovingVariableValueSource source = variableName == null ? null : sources.get(variableName);
    if(source == null) {
      source = new OutlierRemovingVariableValueSource(getWrappedValueTable(), from, statisticsProvider) {
        @Override
        public Value getValue(ValueSet valueSet) {
          return super.getValue(getValueSetMappingFunction().unapply(valueSet));
        }
      };
      sources.put(variableName, source);
    }
    return source;
  }

  private final class WrappingFunction implements BijectiveFunction<VariableValueSource, VariableValueSource> {
    @Override
    public VariableValueSource unapply(VariableValueSource from) {
      if(from instanceof OutlierRemovingVariableValueSource) {
        return ((OutlierRemovingVariableValueSource) from).getWrappedSource();
      }
      return ((VariableValueSourceWrapper) from).getWrapped();
    }

    @Override
    public VariableValueSource apply(VariableValueSource from) {
      if(canRemoveOutliers(from)) {
        return cacheLookup(from);
      }
      return new VariableValueSourceWrapper(from);
    }
  }

}
