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

public class IncompatibleValueTypeException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  private final String variableName;

  private final ValueType originalValueType;

  private final ValueType valueType;

  public IncompatibleValueTypeException(String variableName, ValueType originalValueType, ValueType valueType) {
    super("Incompatible value types for variable [" + variableName + "]: '" + originalValueType + "' / '" + valueType + "'");
    this.variableName = variableName;
    this.originalValueType = originalValueType;
    this.valueType = valueType;
  }

  public String getVariableName() {
    return variableName;
  }

  public ValueType getOriginalValueType() {
    return originalValueType;
  }

  public ValueType getValueType() {
    return valueType;
  }
}
