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

public class NoSuchValueSetException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  private final String table;

  private final String entity;

  public NoSuchValueSetException(String table, String entity) {
    super("No ValueSet in table '" + table + "' for entity '" + entity + "'");
    this.table = table;
    this.entity = entity;
  }

  public NoSuchValueSetException(ValueTable table, VariableEntity entity) {
    this(table.getName(), entity.toString());
  }

  public String getTable() {
    return table;
  }

  public String getEntity() {
    return entity;
  }
}
