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

  private final String tableEntityType;

  private final String variableEntityType;

  private final String variableName;

  public IncompatibleEntityTypeException(String variableName, String tableEntityType, String variableEntityType) {
    super("Incompatible entity types for variable [" + variableName + "]: '" + tableEntityType + "' / '" + variableEntityType + "'");
    this.tableEntityType = tableEntityType;
    this.variableEntityType = variableEntityType;
    this.variableName = variableName;
  }

  public String getVariableName() {
    return variableName;
  }

  @Deprecated
  public String getViewEntityType() {
    return getTableEntityType();
  }

  public String getTableEntityType() {
    return tableEntityType;
  }

  public String getVariableEntityType() {
    return variableEntityType;
  }
}
