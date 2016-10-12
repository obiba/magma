/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views.support;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.WhereClause;

public final class AllClause implements SelectClause, WhereClause {
  //
  // SelectClause Methods
  //

  @Override
  public boolean select(Variable variable) {
    return true;
  }

  //
  // WhereClause Methods
  //

  @Override
  public boolean where(ValueSet valueSet) {
    return true;
  }

  @Override
  public boolean where(ValueSet valueSet, View view) {
    return true;
  }
}
