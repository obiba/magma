package org.obiba.magma.support;

import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.transform.BijectiveFunctions;
import org.obiba.magma.views.AbstractTransformingValueTableWrapper;

import com.google.common.collect.Sets;

/**
 * Transforming value table that limit the number of entities reported by the wrapped @{link ValueTable}
 */
public class BatchValueTable extends AbstractTransformingValueTableWrapper {

  private final ValueTable wrapped;

  private final BijectiveFunction<VariableEntity, VariableEntity> variableEntityMappingFunction;

  public BatchValueTable(ValueTable sourceTable, int limit) {
    wrapped = sourceTable;
    variableEntityMappingFunction = limit < 0
        ? BijectiveFunctions.<VariableEntity>identity()
        : new BatchFunction(limit);
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return wrapped;
  }

  @NotNull
  @Override
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return variableEntityMappingFunction;
  }

  private static class BatchFunction implements BijectiveFunction<VariableEntity, VariableEntity> {

    private final int limit;

    private final Set<VariableEntity> entities = Sets.newHashSet();

    private BatchFunction(int limit) {
      this.limit = limit;
    }

    @Override
    public VariableEntity unapply(VariableEntity from) {
      return entities.contains(from) ? from : null;
    }

    @Nullable
    @Override
    public VariableEntity apply(@Nullable VariableEntity input) {
      if(entities.size() < limit) {
        entities.add(input);
        return input;
      }
      if(entities.contains(input)) {
        return input;
      }
      return null;
    }
  }
}
