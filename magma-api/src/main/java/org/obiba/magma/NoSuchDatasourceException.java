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

import javax.validation.constraints.NotNull;

public class NoSuchDatasourceException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  @NotNull
  private final String datasourceName;

  public NoSuchDatasourceException(@NotNull String datasourceName) {
    super("No datasource exists with the specified name '" + datasourceName + "'");
    this.datasourceName = datasourceName;
  }

  @NotNull
  public String getDatasourceName() {
    return datasourceName;
  }

}
