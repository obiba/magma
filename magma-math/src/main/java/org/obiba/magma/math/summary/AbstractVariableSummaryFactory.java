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

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public abstract class AbstractVariableSummaryFactory<TVariableSummary extends VariableSummary>
    implements VariableSummaryFactory<TVariableSummary> {

  private Variable variable;

  private ValueTable table;

  private ValueSource valueSource;

  @Override
  @NotNull
  public Variable getVariable() {
    return variable;
  }

  @Override
  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  @Override
  @NotNull
  public ValueTable getTable() {
    return table;
  }

  @Override
  public void setTable(ValueTable table) {
    this.table = table;
  }

  @NotNull
  @Override
  public ValueSource getValueSource() {
    return valueSource;
  }

  @Override
  public void setValueSource(ValueSource valueSource) {
    this.valueSource = valueSource;
  }

}
