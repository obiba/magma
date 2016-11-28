/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss.support;

import com.google.common.collect.Lists;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.TextType;
import org.opendatafoundation.data.spss.SPSSVariable;

import java.util.List;

import static org.obiba.magma.datasource.spss.support.CharacterSetValidator.validate;

public abstract class SpssValueFactory {

  private final boolean withValidation;

  private final List<Integer> valuesIndex;

  protected final SPSSVariable spssVariable;

  private final ValueType valueType;

  protected final boolean repeatable;

  private SpssTypeFormatter valueFormatter;

  public SpssValueFactory(List<Integer> valuesIndex, SPSSVariable spssVariable, ValueType valueType, boolean withValidation, boolean repeatable) {
    this.valuesIndex = valuesIndex;
    this.spssVariable = spssVariable;
    this.valueType = valueType;
    this.withValidation = withValidation;
    this.repeatable = repeatable;
    initializeVariableTypeFormatter();
  }

  public Value create() {
    if (repeatable) {
      List<Value> values = Lists.newArrayListWithCapacity(valuesIndex.size());
      valuesIndex.forEach(index -> values.add(createValue(index)));
      return valueType.sequenceOf(values);
    } else {
      return createValue(valuesIndex.get(0));
    }
  }

  private Value createValue(int index) {
    String value = getValue(index);
    if (withValidation) {
      try {
        validate(value);
      } catch(SpssInvalidCharacterException e) {
        String variableName = spssVariable.getName();
        throw new SpssDatasourceParsingException("Invalid characters in variable value.", "InvalidCharsetCharacter",
            index, e.getSource()).dataInfo(variableName, index).extraInfo(e);
      } catch(MagmaRuntimeException e) {
        String variableName = spssVariable.getName();
        throw new SpssDatasourceParsingException("Failed to create variable value", "SpssFailedToCreateVariable",
            variableName, index).dataInfo(variableName, index).extraInfo(e.getMessage());
      }
    }
    return valueType.valueOf(valueFormatter.format(value));
  }

  protected abstract String getValue(int index);

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
