/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import java.util.Set;

import javax.validation.constraints.NotNull;

import com.google.common.base.Predicate;

public interface Datasource extends Initialisable, Disposable, AttributeAware, Timestamped {

  String getName();

  String getType();

  boolean hasValueTable(String tableName);

  boolean hasEntities(Predicate<ValueTable> predicate);

  ValueTable getValueTable(String tableName) throws NoSuchValueTableException;

  Set<ValueTable> getValueTables();

  boolean canDropTable(String tableName);

  void dropTable(String tableName);

  boolean canRenameTable(String tableName);

  void renameTable(String tableName, String newName);

  void drop();

  boolean canDrop();

  @NotNull
  ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType);

  void setAttributeValue(String name, Value value);

  boolean isTransactional();
}
