/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.crypt;

import org.obiba.magma.MagmaRuntimeException;

/**
 * Base class for <code>KeyProvider</code> exceptions.
 */
public class MagmaCryptRuntimeException extends MagmaRuntimeException {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Constructors
  //

  public MagmaCryptRuntimeException(Throwable cause) {
    super(cause);
  }

  public MagmaCryptRuntimeException(String message) {
    super(message);
  }
}
