/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;

import com.google.common.base.Predicate;

public final class EntitiesPredicate {

  private EntitiesPredicate() {}

  public static class DefaultEntitiesPredicate implements Predicate<ValueTable> {
    @Override
    public boolean apply(@NotNull ValueTable value) {
      return true;
    }
  }

  public static class NonViewEntitiesPredicate implements Predicate<ValueTable> {
    @Override
    public boolean apply(@NotNull ValueTable valueTable) {
      return !valueTable.isView() && !valueTable.getVariableEntities().isEmpty();
    }
  }

  public static class ViewsOnlyEntitiesPredicate implements Predicate<ValueTable> {
    @Override
    public boolean apply(@NotNull ValueTable valueTable) {
      return valueTable.isView() && !valueTable.getVariableEntities().isEmpty();
    }
  }
}
