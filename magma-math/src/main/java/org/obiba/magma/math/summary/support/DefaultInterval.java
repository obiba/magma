/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math.summary.support;

import org.obiba.magma.math.Interval;

public class DefaultInterval implements Interval {

  private double lower;
  private double upper;
  private long freq;
  private double density;

  @Override
  public double getLower() {
    return lower;
  }

  public void setLower(double lower) {
    this.lower = lower;
  }

  @Override
  public double getUpper() {
    return upper;
  }

  public void setUpper(double upper) {
    this.upper = upper;
  }

  @Override
  public long getFreq() {
    return freq;
  }

  public void setFreq(long freq) {
    this.freq = freq;
  }

  @Override
  public double getDensity() {
    return density;
  }

  public void setDensity(double density) {
    this.density = density;
  }
}
