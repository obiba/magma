/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.crypt;

import org.obiba.magma.MagmaRuntimeException;

public class MagmaCryptRuntimeException extends MagmaRuntimeException {

  private static final long serialVersionUID = 1L;

  public MagmaCryptRuntimeException(Throwable cause) {
    super(cause);
  }

  public MagmaCryptRuntimeException(String message) {
    super(message);
  }

  public MagmaCryptRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
