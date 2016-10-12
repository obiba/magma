/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
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

public interface VariableSummaryFactory<TVariableSummary extends VariableSummary> {

  @NotNull
  TVariableSummary getSummary();

  @NotNull
  String getCacheKey();

  @NotNull
  Variable getVariable();

  @NotNull
  ValueTable getTable();

  void setValueSource(ValueSource valueSource);

  @NotNull
  ValueSource getValueSource();

  void setTable(ValueTable table);

  void setVariable(Variable variable);
}
