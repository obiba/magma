/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.limesurvey;

public class LimesurveyUtils {
  private LimesurveyUtils() {}

  /**
   * Remove ':' and '.'
   *
   * @param name
   * @return
   */
  public static String toValidMagmaName(String name) {
    return name.replaceAll(":", "").replaceAll("\\.", "");
  }

  /**
   * Remove slashes
   *
   * @param name
   * @return
   */
  public static String removeSlashes(String name) {
    return name.replaceAll("/", "");
  }
}
