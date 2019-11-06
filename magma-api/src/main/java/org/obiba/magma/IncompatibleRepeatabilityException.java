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

public class IncompatibleRepeatabilityException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  private final String variableName;

  private final boolean originalRepeatable;

  private final boolean repeatable;

  public IncompatibleRepeatabilityException(String variableName, boolean originalRepeatable, boolean repeatable) {
    super("Incompatible repeatability for variable [" + variableName + "]: '" + originalRepeatable + "' / '" + repeatable + "'");
    this.variableName = variableName;
    this.originalRepeatable = originalRepeatable;
    this.repeatable = repeatable;
  }

  public String getVariableName() {
    return variableName;
  }

  public boolean isOriginalRepeatable() {
    return originalRepeatable;
  }

  public boolean isRepeatable() {
    return repeatable;
  }
}
