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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class NoSuchVariableException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  @Nullable
  private String valueTableName;

  @NotNull
  private final String name;

  public NoSuchVariableException(@Nullable String valueTableName, @NotNull String name) {
    super("No such variable '" + name + "' in table '" + valueTableName + "'");
    this.valueTableName = valueTableName;
    this.name = name;
  }

  public NoSuchVariableException(@NotNull String name) {
    super("No such variable '" + name + "'");
    this.name = name;
  }

  @Nullable
  public String getValueTableName() {
    return valueTableName;
  }

  @NotNull
  public String getName() {
    return name;
  }
}
