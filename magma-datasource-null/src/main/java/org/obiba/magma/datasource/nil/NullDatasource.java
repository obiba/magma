package org.obiba.magma.datasource.nil;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

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
  @Nonnull
  public ValueTableWriter createWriter(String name, String entityType) {
    return new NullValueTableWriter();
  }

  private static class NullValueTableWriter implements ValueTableWriter {

    @Nonnull
    @Override
    public ValueSetWriter writeValueSet(@Nonnull VariableEntity entity) {
      return new ValueSetWriter() {

        @Override
        public void close() throws IOException {
        }

        @Override
        public void writeValue(@Nonnull Variable variable, Value value) {
        }
      };
    }

    @Override
    public VariableWriter writeVariables() {
      return new VariableWriter() {

        @Override
        public void close() throws IOException {
        }

        @Override
        public void writeVariable(@Nonnull Variable variable) {
        }
      };
    }

    @Override
    public void close() throws IOException {
    }

  }

}
