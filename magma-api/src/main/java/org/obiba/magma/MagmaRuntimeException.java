/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

/**
 * Base class for all Magma runtime exceptions. Any exception thrown by Magma runtime should extend this class.
 */
public class MagmaRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -1825626210129821160L;

  public MagmaRuntimeException() {
  }

  public MagmaRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public MagmaRuntimeException(String message) {
    super(message);
  }

  public MagmaRuntimeException(Throwable cause) {
    super(cause);
  }

}
