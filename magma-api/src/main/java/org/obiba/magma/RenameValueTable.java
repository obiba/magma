/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import javax.validation.constraints.NotNull;

import org.obiba.magma.support.AbstractValueTableWrapper;

/**
 * A {@link org.obiba.magma.support.ValueTableWrapper} that allows to rename the value table without
 * affecting the set of variables and entities.
 */
public class RenameValueTable extends AbstractValueTableWrapper {

  private final String name;

  private final ValueTable wrapped;

  public RenameValueTable(String name, ValueTable wrapped) {
    this.name = name;
    this.wrapped = wrapped;
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return wrapped;
  }

  @NotNull
  @Override
  public String getName() {
    return name;
  }
}
