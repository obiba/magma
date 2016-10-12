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

public interface ValueTableWriter extends AutoCloseable {

  VariableWriter writeVariables();

  @NotNull
  ValueSetWriter writeValueSet(@NotNull VariableEntity entity);

  @Override
  void close();

  interface VariableWriter extends AutoCloseable {

    void writeVariable(@NotNull Variable variable);

    void removeVariable(@NotNull Variable variable);

    @Override
    void close();

  }

  interface ValueSetWriter extends AutoCloseable {

    void writeValue(@NotNull Variable variable, Value value);

    void remove();

    @Override
    void close();

  }

}
