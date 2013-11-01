package org.obiba.magma.support;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTable;

import com.google.common.base.Predicate;

public final class EntitiesPredicate {

  private EntitiesPredicate() {}

  public static class DefaultEntitiesPredicate implements Predicate<ValueTable> {
    @Override
    public boolean apply(@Nonnull ValueTable value) {
      return true;
    }
  }

  public static class NonViewEntitiesPredicate implements Predicate<ValueTable> {
    @Override
    public boolean apply(@Nonnull ValueTable valueTable) {
      return !valueTable.isView() && !valueTable.getVariableEntities().isEmpty();
    }
  }

  public static class ViewsOnlyEntitiesPredicate implements Predicate<ValueTable> {
    @Override
    public boolean apply(@Nonnull ValueTable valueTable) {
      return valueTable.isView() && !valueTable.getVariableEntities().isEmpty();
    }
  }
}
