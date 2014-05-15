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

/**
 * Signals some type of <code>KeyProvider</code> security exception.
 */
public class KeyProviderSecurityException extends MagmaCryptRuntimeException {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Constructors
  //

  public KeyProviderSecurityException(Throwable cause) {
    super(cause);
  }

  public KeyProviderSecurityException(String message) {
    super(message);
  }
}
