/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss.support;

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.TextType;
import org.opendatafoundation.data.spss.SPSSVariable;

import static org.obiba.magma.datasource.spss.support.CharacterSetValidator.validate;

public abstract class SpssValueFactory {

  protected final boolean withValidation;

  protected final int variableIndex;

  protected final SPSSVariable spssVariable;

  protected final ValueType valueType;

  private SpssTypeFormatter valueFormatter;

  public SpssValueFactory(int variableIndex, SPSSVariable spssVariable, ValueType valueType, boolean withValidation) {
    this.variableIndex = variableIndex;
    this.spssVariable = spssVariable;
    this.valueType = valueType;
    this.withValidation = withValidation;
    initializeVariableTypeFormatter();
  }

  public abstract Value create();

  protected Value createValue() throws SpssInvalidCharacterException {
    String value = getValue();
    if (withValidation) {
      validate(value);
    }
    return valueType.valueOf(valueFormatter.format(value));
  }

  protected abstract String getValue();

  @SuppressWarnings("PMD.NcssMethodCount")
  private void initializeVariableTypeFormatter() {

    if(valueType instanceof TextType) {
      valueFormatter = new SpssDefaultTypeFormatter();
      return;
    }

    switch(SpssVariableTypeMapper.getSpssNumericDataType(spssVariable)) {
      case DOT:
        valueFormatter = new SpssDotTypeFormatter();
        break;

      case COMMA:
        valueFormatter = new SpssCommaTypeFormatter();
        break;

      case DOLLAR:
        valueFormatter = new SpssDollarTypeFormatter();
        break;

      case FIXED:
      case SCIENTIFIC:
        valueFormatter = new SpssNumberTypeFormatter();
        break;

      default:
        valueFormatter = new SpssDefaultTypeFormatter();
    }

  }

  //
  // Inner classes
  //

  private interface SpssTypeFormatter {
    String format(String value);
  }

  private static class SpssDefaultTypeFormatter implements SpssTypeFormatter {
    @Override
    public String format(String value) {
      String trimmed = value.trim();
      return trimmed.isEmpty() ? null : trimmed;
    }
  }

  private static class SpssNumberTypeFormatter extends SpssDefaultTypeFormatter {
    @Override
    public String format(String value) {
      return super.format(value.replaceAll("\\*", "")); // removes overflow delimeter if any
    }
  }

  private static class SpssDotTypeFormatter extends SpssNumberTypeFormatter {
    @Override
    public String format(String value) {
      return super.format(value.replaceAll("\\.", "").replaceAll(",", "."));
    }
  }

  private static class SpssCommaTypeFormatter extends SpssNumberTypeFormatter {
    @Override
    public String format(String value) {
      return super.format(value.replaceAll(",", ""));
    }
  }

  private static class SpssDollarTypeFormatter extends SpssNumberTypeFormatter {
    @Override
    public String format(String value) {
      return super.format(value.replaceAll("\\$|,", ""));
    }
  }
}
