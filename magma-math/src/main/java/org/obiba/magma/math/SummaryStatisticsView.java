package org.obiba.magma.math;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.VectorSourceNotSupportedException;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.DecimalType;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * A {@code ValueTable} implementation that will compute a statistical summary for all numerical variables of another
 * table. Entities of this table are the {@code Variables} of the other. The variables of this table are the available
 * univariate statistics (mean, min, max, sum, etc.).
 */
@SuppressWarnings("UnusedDeclaration")
public class SummaryStatisticsView extends AbstractValueTable implements Initialisable {

  private final ValueTable valueTable;

  private final DescriptiveStatisticsProvider statsProvider = new DefaultDescriptiveStatisticsProvider();

  public SummaryStatisticsView(Datasource ds, String name, ValueTable valueTable) {
    super(ds, name);
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    this.valueTable = valueTable;
  }

  @Override
  public void initialise() {
    // Each variable in the wrapped table becomes a valueSet in this table
    setVariableEntityProvider(new AggregateVariableEntityProvider());
    addVariableValueSources(ImmutableSet.<VariableValueSource>of(//
        new StatVariableValueSource("Min"), new StatVariableValueSource("Max"), new StatVariableValueSource("Mean"),//
        new StatVariableValueSource("GeometricMean"), new StatVariableValueSource("n"),
        new StatVariableValueSource("Sum"),//
        new StatVariableValueSource("SumSq"), new StatVariableValueSource("StandardDeviation"),
        new StatVariableValueSource("Variance"),//
        new StatVariableValueSource("Skewness"), new StatVariableValueSource("Kurtosis")));
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return NullTimestamps.get();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new AggregateValueSet(entity);
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return NullTimestamps.get();
  }

  private class AggregateValueSet extends ValueSetBean {

    private final DescriptiveStatistics ds;

    protected AggregateValueSet(VariableEntity entity) {
      super(SummaryStatisticsView.this, entity);
      ds = statsProvider.compute(valueTable.getVariableValueSource(entity.getIdentifier()),
          Sets.newTreeSet(valueTable.getVariableEntities()));
    }

    DescriptiveStatistics getStats() {
      return ds;
    }

  }

  private class StatVariableValueSource extends AbstractVariableValueSource implements VariableValueSource {

    private final String statName;

    private final Method getter;

    private StatVariableValueSource(String name) {
      statName = name;
      getter = Iterables.find(Arrays.asList(DescriptiveStatistics.class.getMethods()), new Predicate<Method>() {

        @Override
        public boolean apply(Method input) {
          return input.getName().equalsIgnoreCase("get" + statName);
        }

      });
    }

    @NotNull
    @Override
    public Variable getVariable() {
      return Variable.Builder.newVariable(statName, DecimalType.get(), getEntityType()).build();
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      try {
        return DecimalType.get().valueOf(getter.invoke(((AggregateValueSet) valueSet).getStats()));
      } catch(Exception e) {
        throw new RuntimeException(e);
      }
    }

    @NotNull
    @Override
    public ValueType getValueType() {
      return DecimalType.get();
    }

    @Override
    public boolean supportVectorSource() {
      return false;
    }

    @NotNull
    @Override
    public VectorSource asVectorSource() {
      throw new VectorSourceNotSupportedException(getClass());
    }
  }

  private class AggregateVariableEntityProvider implements VariableEntityProvider {

    @NotNull
    @Override
    public String getEntityType() {
      return "Variable";
    }

    @NotNull
    @Override
    public Set<VariableEntity> getVariableEntities() {
      return ImmutableSet.copyOf(Iterables
          .transform(Iterables.filter(valueTable.getVariables(), new UnivariateFilter()), new VariableToEntity()));
    }

    @Override
    public boolean isForEntityType(String entityType) {
      return getEntityType().equals(entityType);
    }

  }

  /**
   * Transforms a Variable to a VariableEntity
   */
  private class VariableToEntity implements Function<Variable, VariableEntity> {

    @Override
    public VariableEntity apply(Variable from) {
      return new VariableEntityBean(getEntityType(), from.getName());
    }

  }

  /**
   * Returns true when a variable's type is numeric
   */
  private static class UnivariateFilter implements Predicate<Variable> {

    @Override
    public boolean apply(Variable input) {
      return input.getValueType().isNumeric();
    }

  }
}
