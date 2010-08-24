package org.obiba.magma.math;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.views.View;

/**
 * A {@code View} that removes outlier values from the underlying table. Note that outliers can only be removed for
 * variables that have a numerical value type ({@code ValueType#isNumeric()} returns true).
 * @see OutlierRemovingVariableValueSource
 */
public class OutlierRemovingView extends View {

  private final WrappingFunction function = new WrappingFunction();

  private DescriptiveStatisticsProvider statisticsProvider = new ExcludeMissingDescriptiveStatisticsProvider();

  @Override
  public BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction() {
    return function;
  }

  protected boolean canRemoveOutliers(VariableValueSource source) {
    return source.asVectorSource() != null && source.getVariable().isRepeatable() == false && source.getValueType().isNumeric();
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
        return new OutlierRemovingVariableValueSource(OutlierRemovingView.this.getWrappedValueTable(), from, statisticsProvider) {
          @Override
          public Value getValue(ValueSet valueSet) {
            return super.getValue(getValueSetMappingFunction().unapply(valueSet));
          }
        };
      }
      return new VariableValueSourceWrapper(from);
    }
  }

}
