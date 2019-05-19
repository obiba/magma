/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views.support;

import java.util.Collections;

import javax.validation.constraints.NotNull;

import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.views.ListClause;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.WhereClause;

/**
 * An empty Clause that contains no values.
 */
public final class NoneClause implements SelectClause, WhereClause, ListClause {

  @Override
  public boolean select(Variable variable) {
    return false;
  }

  @Override
  public boolean where(ValueSet valueSet) {
    return false;
  }

  @Override
  public boolean where(ValueSet valueSet, View view) {
    return false;
  }

  @NotNull
  @Override
  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    throw new NoSuchVariableException("VariableValueSource [" + name + "] not found.");
  }

  @Override
  public Iterable<VariableValueSource> getVariableValueSources() {
    return Collections.emptySet();
  }

  @Override
  public void setValueTable(ValueTable valueTable) {
    // No action take for this method.
  }

  @Override
  public VariableWriter createWriter() {
    throw new UnsupportedOperationException();
  }
}
