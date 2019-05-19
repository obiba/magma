/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.js;

import javax.annotation.Nullable;

/**
 * Utility class for Rhino-related concerns
 */
public final class Rhino {

  private Rhino() {}

  /**
   * Fix for Rhino bug 448499. If the input is a Double value of 1.0 or 0.0, this method will convert it to its integer
   * value.
   *
   * @param value a value coming from a Rhino evaluation.
   * @return the value untouched or converted to an int for specific values
   * @see https://bugzilla.mozilla.org/show_bug.cgi?id=448499
   */
  @Nullable
  public static Object fixRhinoNumber(@Nullable Object value) {
    if(value == null) return null;

    Object newValue = value;
    if(value instanceof Double) {
      if((Double) value == 1.0d) {
        newValue = 1;
      } else if((Double) value == 0.0d) {
        newValue = 0;
      }
    }
    return newValue;
  }
}
