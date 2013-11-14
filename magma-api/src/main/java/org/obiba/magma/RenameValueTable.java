package org.obiba.magma;

import javax.annotation.Nonnull;

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

  @Nonnull
  @Override
  public String getName() {
    return name;
  }
}
