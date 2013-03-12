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

import java.util.HashMap;
import java.util.Map;

public enum SpssNumericDataType {

  UNKNOWN(-1),
  COMMA(3),
  DOLLAR(4),
  FIXED(5),
  SCIENTIFIC(17),
  DATE(20), // Date dd-mmm-yyyy or dd-mmm-yy
  TIME(21), // Time in hh:mm, hh:mm:ss or hh:mm:ss.ss
  DATETIME(22), // DateTime in dd-mmm-yyyy hh:mm, dd-mmm-yyyy hh:mm:ss or dd-mmm-yyyy hh:mm:ss.ss
  ADATE(23), // Date in mm/dd/yy or mm/dd/yyyy
  JDATE(24), // Date in yyyyddd or yyddd
  DTIME(25), // // DateTime in ddd:hh:mm, ddd:hh:mm:ss or ddd:hh:mm:ss.ss

  WEEK_DAY(26), // Date as day of the week, full name or 3-letter
  MONTH(27), // Date 3-letter month
  MONTH_YEAR(28), // Date in mmm yyyy or mmm yy
  QUARTERLY_YEAR(29), // Date in q Q yyyy or q Q yy
  WEEK_YEAR(30), // Date in wk WK yyyy or wk WK yy
  DOT(32),
  CUSTOM_CURRENCY_A(33), // Custom currency A
  CUSTOM_CURRENCY_B(34), // Custom currency B
  CUSTOM_CURRENCY_C(35), // Custom currency C
  CUSTOM_CURRENCY_D(36), // Custom currency D
  CUSTOM_CURRENCY_E(37), // Custom currency E
  EDATE(38), // Date in dd.mm.yy or dd.mm.yyyy
  SDATE(39); // Date in yyyy/mm/dd or yy/mm/dd (?)

  public static SpssNumericDataType fromInt(int i) {
    SpssNumericDataType type = intToTypeMap.get(Integer.valueOf(i));

    if(type == null) {
      return SpssNumericDataType.UNKNOWN;
    }

    return type;
  }

  //
  // Enum private members
  //
  private static final Map<Integer, SpssNumericDataType> intToTypeMap = new HashMap<Integer, SpssNumericDataType>();

  static {
    for(SpssNumericDataType type : SpssNumericDataType.values()) {
      intToTypeMap.put(type.value, type);
    }
  }

  //
  // Private members
  //
  private final int value;

  private SpssNumericDataType(int value) {
    this.value = value;
  }

}
