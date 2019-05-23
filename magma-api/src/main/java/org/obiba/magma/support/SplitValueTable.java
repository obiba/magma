package org.obiba.magma.support;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * Split a value table by row: subset the entities.
 */
public class SplitValueTable extends AbstractValueTableWrapper {

  private final String name;

  private final ValueTable wrapped;

  private final List<VariableEntity> entities;

  /**
   * Constructor of a value table with a subset of entities. Make sure this subset applies to the
   * wrapped table.
   *
   * @param wrapped
   * @param entities
   */
  public SplitValueTable(ValueTable wrapped, Iterable<VariableEntity> entities) {
    this(wrapped.getName(), wrapped, entities);
  }

  /**
   * Constructor of a value table with a subset of entities. Make sure this subset applies to the
   * wrapped table.
   *
   * @param name     New name, if null or empty the wrapped name is used.
   * @param wrapped
   * @param entities
   */
  public SplitValueTable(String name, ValueTable wrapped, Iterable<VariableEntity> entities) {
    this.name = name;
    this.wrapped = wrapped;
    this.entities = Lists.newArrayList(entities);
  }

  @NotNull
  @Override
  public String getName() {
    return Strings.isNullOrEmpty(name) ? super.getName() : name;
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return wrapped;
  }

  @Override
  public List<VariableEntity> getVariableEntities() {
    return entities;
  }
}
