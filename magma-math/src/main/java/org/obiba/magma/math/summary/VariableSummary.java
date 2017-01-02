/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math.summary;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public interface VariableSummary extends Serializable {

  String getCacheKey(ValueTable table);

  @NotNull
  Variable getVariable();

  @NotNull
  String getVariableName();

}
