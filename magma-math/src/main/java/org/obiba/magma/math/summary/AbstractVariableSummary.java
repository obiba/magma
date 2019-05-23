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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.obiba.magma.*;

import javax.validation.constraints.NotNull;
import java.util.List;

public abstract class AbstractVariableSummary implements VariableSummary {

  private static final long serialVersionUID = 3105572632716973506L;

  @NotNull
  protected transient final Variable variable;

  @NotNull
  protected final String variableName;

  protected Integer offset;

  protected Integer limit;

  protected AbstractVariableSummary(@NotNull Variable variable) {
    //noinspection ConstantConditions
    Preconditions.checkArgument(variable != null, "Variable cannot be null");
    this.variable = variable;
    variableName = variable.getName();
  }

  protected List<VariableEntity> getFilteredVariableEntities(@NotNull ValueTable table) {
    if (offset == null && limit == null) return table.getVariableEntities();

    Iterable<VariableEntity> entities = table.getVariableEntities();
    // Apply offset then limit (in that order)
    if (offset != null) {
      entities = Iterables.skip(entities, offset);
    }
    if (limit != null && limit >= 0) {
      entities = Iterables.limit(entities, limit);
    }
    return ImmutableList.copyOf(entities);
  }

  @NotNull
  @Override
  public String getVariableName() {
    return variableName;
  }

  @NotNull
  @Override
  public Variable getVariable() {
    return variable;
  }

  void setOffset(Integer offset) {
    this.offset = offset;
  }

  void setLimit(Integer limit) {
    this.limit = limit;
  }

  public boolean isFiltered() {
    return offset != null || limit != null;
  }

  public Integer getOffset() {
    return offset;
  }

  public Integer getLimit() {
    return limit;
  }

  public interface VariableSummaryBuilder<TVariableSummary extends VariableSummary, TVariableSummaryBuilder extends VariableSummaryBuilder<TVariableSummary, TVariableSummaryBuilder>> {

    TVariableSummary build();

    @NotNull
    Variable getVariable();

    TVariableSummaryBuilder addTable(@NotNull ValueTable table, @NotNull ValueSource variableValueSource);

    TVariableSummaryBuilder addValue(@NotNull Value value);
  }

}
