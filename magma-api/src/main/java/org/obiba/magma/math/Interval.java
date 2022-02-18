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

public interface Interval extends Serializable {

  /**
   * Interval lower bound.
   *
   * @return
   */
  double getLower();

  /**
   * Interval upper bound.
   *
   * @return
   */
  double getUpper();

  /**
   * Frequency in the interval.
   *
   * @return
   */
  long getFreq();

  /**
   * Density in the interval.
   *
   * @return
   */
  double getDensity();

}
