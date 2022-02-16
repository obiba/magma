/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math;

import java.io.Serializable;

public interface Frequency extends Serializable {

  /**
   * The name that designate the value (or the set of values) being counted.
   *
   * @return
   */
  String getValue();

  /**
   * The count.
   *
   * @return
   */
  long getFreq();

  /**
   * The percentage.
   *
   * @return
   */
  double getPct();

  /**
   * Whether the value represents a missing one.
   *
   * @return
   */
  boolean isMissing();

}
