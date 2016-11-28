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

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

import java.util.List;

public class SpssVariableValueFactory extends SpssValueFactory {

  public SpssVariableValueFactory(List<Integer> valuesIndex, SPSSVariable spssVariable, ValueType valueType, boolean withValidation, boolean repeatable) {
    super(valuesIndex, spssVariable, valueType, withValidation, repeatable);
  }

  @Override
  protected String getValue(int index) {
    try {
      String value = spssVariable.getValueAsString(index, new FileFormatInfo(FileFormatInfo.Format.ASCII));
      return SpssVariableValueConverter.convert(spssVariable, value);
    } catch(SPSSFileException | SpssValueConversionException e) {
      String variableName = spssVariable.getName();
      throw new SpssDatasourceParsingException("Failed to retieve variable value.", "SpssFailedToCreateVariable",
          variableName, index).dataInfo(variableName, index).extraInfo(e.getMessage());
    }
  }
}
