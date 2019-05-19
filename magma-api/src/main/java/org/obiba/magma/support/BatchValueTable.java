/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

  @Override
  public int getVariableEntityCount() {
    // get the exact batch size
    if (getVariableEntityMappingFunction() instanceof BatchFunction) {
      BatchFunction batchFunction = (BatchFunction) getVariableEntityMappingFunction();
      if (!batchFunction.isApplied()) {
        getVariableEntities();
      }
      return batchFunction.getCount();
    }
    return super.getVariableEntityCount();
  }

  @NotNull
  @Override
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return variableEntityMappingFunction;
  }

  private static class BatchFunction implements BijectiveFunction<VariableEntity, VariableEntity> {

    private final int limit;

    private final Set<VariableEntity> entities = Sets.newHashSet();

    private boolean applied = false;

    private BatchFunction(int limit) {
      this.limit = limit;
    }

    private boolean isApplied() {
      return applied;
    }

    public int getCount() {
      return entities.size();
    }

    @Override
    public VariableEntity unapply(VariableEntity from) {
      return entities.contains(from) ? from : null;
    }

    @Nullable
    @Override
    public VariableEntity apply(@Nullable VariableEntity input) {
      applied = true;
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
