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

public class IncompatibleEntityTypeException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  private final String viewEntityType;

  private final String variableEntityType;

  public IncompatibleEntityTypeException(String viewEntityType, String variableEntityType) {
    super("Incompatible entity types: '" + viewEntityType + "' / '" + variableEntityType + "'");
    this.viewEntityType = viewEntityType;
    this.variableEntityType = variableEntityType;
  }

  public String getViewEntityType() {
    return viewEntityType;
  }

  public String getVariableEntityType() {
    return variableEntityType;
  }
}
