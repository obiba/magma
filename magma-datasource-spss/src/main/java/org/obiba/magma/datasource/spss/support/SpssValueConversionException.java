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

import com.google.common.base.Strings;

public class SpssValueConversionException extends Exception {

  private static final long serialVersionUID = 8229333185779111603L;

  public SpssValueConversionException(String message, String value) {
    super(message + formatValue(value));
  }

  public SpssValueConversionException(String message, Throwable throwable, String value) {
    super(message + formatValue(value), throwable);
  }

  private static String formatValue(String value) {
    return " :'" + Strings.nullToEmpty(value) + "'";
  }
}
