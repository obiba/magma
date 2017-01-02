/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.spss.support;

import org.opendatafoundation.data.spss.SPSSStringVariable;
import org.opendatafoundation.data.spss.SPSSVariable;

import com.google.common.base.Strings;

public class SpssVariableValueConverter {

  private SpssVariableValueConverter() {}

  public static String convert(SPSSVariable spssVariable, String value) throws SpssValueConversionException {
    String trimmedValue = value.trim();
    if(Strings.isNullOrEmpty(trimmedValue) || (spssVariable instanceof SPSSStringVariable)) return value;

    switch(SpssVariableTypeMapper.getSpssNumericDataType(spssVariable)) {
      case ADATE:
        return new SpssDateValueConverters.ADateValueConverter().convert(trimmedValue);
      case DATE:
        return new SpssDateValueConverters.DateValueConverter().convert(trimmedValue);
      case DATETIME:
        return new SpssDateValueConverters.DateTimeValueConverter().convert(trimmedValue);
      default:
        return value;
    }
  }

}
