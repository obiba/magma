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
