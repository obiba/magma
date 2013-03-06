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
import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

import static org.obiba.magma.datasource.spss.support.CharacterSetValidator.validate;

public class SpssValueFactory {

  private final int variableIndex;

  private final SPSSVariable spssVariable;

  private final ValueType valueType;

  private SpssTypeFormatter valueFormatter;

  public SpssValueFactory(int variableIndex, SPSSVariable spssVariable, ValueType valueType) {
    this.variableIndex = variableIndex;
    this.spssVariable = spssVariable;
    this.valueType = valueType;
    initializeVariableTypeFormatter();
  }

  public Value create() {
    try {
      String rawValue = spssVariable.getValueAsString(variableIndex, new FileFormatInfo(FileFormatInfo.Format.ASCII));
      validate(rawValue);
      return valueType.valueOf(valueFormatter.format(rawValue));
    } catch(SPSSFileException e) {
      throw new SpssDatasourceParsingException(e, "TableDefinitionErrors", spssVariable.getName());
    } catch(SpssIsoControlCharacterException e) {
      throw new SpssDatasourceParsingException("Failed to create variable", spssVariable.getName(), variableIndex,
          "InvalidCharsetCharacter", variableIndex);
    }
  }

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

  private interface SpssTypeFormatter {
    String format(String value);
  }

  private static class SpssNumberTypeFormatter implements SpssTypeFormatter {
    @Override
    public String format(String value) {
      return value.replaceAll(" ", "").isEmpty() ? "0" : value;
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
      return super.format(value.replaceAll(",|", ""));
    }
  }

  private static class SpssDollarTypeFormatter extends SpssNumberTypeFormatter {
    @Override
    public String format(String value) {
      return super.format(value.replaceAll("\\$|,|", ""));
    }
  }

  private static class SpssDefaultTypeFormatter implements SpssTypeFormatter {
    @Override
    public String format(String value) {
      return value;
    }
  }

}
