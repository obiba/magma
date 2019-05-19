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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class NoSuchViewException extends MagmaRuntimeException {

  private static final long serialVersionUID = -4146817951140665348L;

  @NotNull
  private final String view;

  public NoSuchViewException(@Nullable String view) {
    super("No such view exists with the specified name '" + view + "'");
    this.view = view;
  }

  public String getView() {
    return view;
  }
}
