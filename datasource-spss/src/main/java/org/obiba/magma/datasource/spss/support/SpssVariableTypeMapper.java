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

import org.obiba.magma.type.AbstractValueType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.opendatafoundation.data.spss.SPSSNumericVariable;
import org.opendatafoundation.data.spss.SPSSRecordType2;
import org.opendatafoundation.data.spss.SPSSVariable;

public class SpssVariableTypeMapper {

  private SpssVariableTypeMapper() {
  }

  public static AbstractValueType map(SPSSVariable variable)
  {
    return variable instanceof SPSSNumericVariable ? mapNumericType(variable) : TextType.get();
  }

  public static SpssNumericDataType getSpssNumericDataType(SPSSVariable variable) {
    if (!(variable instanceof SPSSNumericVariable)) {
      throw new IllegalArgumentException("Variable must be of type " + SPSSNumericVariable.class);
    }

    SPSSRecordType2 variableRecord = variable.variableRecord;
    return SpssNumericDataType.fromInt(variableRecord.getWriteFormatType());
  }

  private static AbstractValueType mapNumericType(SPSSVariable variable) {

    switch (getSpssNumericDataType(variable)) {
      case COMMA: // comma
      case DOLLAR: // dollar
      case DOT: // dot
      case FIXED: // fixed format (default)
      case SCIENTIFIC: // scientific notation
        return variable.getDecimals() > 0 ? DecimalType.get() : IntegerType.get();

      case DATE: // Date dd-mmm-yyyy or dd-mmm-yy
      case ADATE: // Date in mm/dd/yy or mm/dd/yyyy
      case EDATE: // Date in dd.mm.yy or dd.mm.yyyy
      case SDATE: // Date in yyyy/mm/dd or yy/mm/dd (?)
        return DateType.get();

      case DATETIME: // DateTime in dd-mmm-yyyy hh:mm, dd-mmm-yyyy hh:mm:ss or dd-mmm-yyyy hh:mm:ss.ss
        return DateTimeType.get();

      case TIME: // Time in hh:mm, hh:mm:ss or hh:mm:ss.ss
      case JDATE: // Date in yyyyddd or yyddd
      case DTIME: // DateTime in ddd:hh:mm, ddd:hh:mm:ss or ddd:hh:mm:ss.ss
      case WEEK_DAY: // Date as day of the week, full name or 3-letter
      case MONTH: // Date 3-letter month
      case MONTH_YEAR: // Date in mmm yyyy or mmm yy
      case QUARTERLY_YEAR: // Date in q Q yyyy or q Q yy
      case WEEK_YEAR: // Date in wk WK yyyy or wk WK yy
      case CUSTOM_CURRENCY_A: // Custom currency A
      case CUSTOM_CURRENCY_B: // Custom currency B
      case CUSTOM_CURRENCY_C: // Custom currency C
      case CUSTOM_CURRENCY_D: // Custom currency D
      case CUSTOM_CURRENCY_E: // Custom currency E
      default:
        return TextType.get();
    }
  }
}
