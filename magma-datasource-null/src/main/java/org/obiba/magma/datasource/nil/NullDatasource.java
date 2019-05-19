/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.nil;

import java.util.Collections;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractDatasource;

/**
 * A null {@code Datasource}. This datasource will return a {@code ValueTableWriter} that does nothing. Instances of
 * this class can be useful during testing or for performance debugging.
 */
public class NullDatasource extends AbstractDatasource {

  public NullDatasource(String name) {
    super(name, "null");
  }

  @Override
  protected Set<String> getValueTableNames() {
    return Collections.emptySet();
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return null;
  }

  @Override
  @NotNull
  public ValueTableWriter createWriter(@NotNull String name, @NotNull String entityType) {
    return new NullValueTableWriter();
  }

  private static class NullValueTableWriter implements ValueTableWriter {

    @NotNull
    @Override
    public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
      return new ValueSetWriter() {

        @Override
        public void close() {
        }

        @Override
        public void writeValue(@NotNull Variable variable, Value value) {
        }

        @Override
        public void remove() {

        }
      };
    }

    @Override
    public VariableWriter writeVariables() {
      return new VariableWriter() {

        @Override
        public void close() {
        }

        @Override
        public void writeVariable(@NotNull Variable variable) {
        }

        @Override
        public void removeVariable(@NotNull Variable variable) {
        }
      };
    }

    @Override
    public void close() {
    }

  }

}
