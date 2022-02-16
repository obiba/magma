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

import org.obiba.magma.math.Frequency;

public class DefaultFrequency implements Frequency {

  private final String value;

  private final long freq;

  private final double pct;

  private final boolean missing;

  public DefaultFrequency(String value, long freq, double pct) {
    this(value, freq, pct, false);
  }

  public DefaultFrequency(String value, long freq, double pct, boolean missing) {
    this.value = value;
    this.freq = freq;
    this.pct = pct;
    this.missing = missing;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public long getFreq() {
    return freq;
  }

  @Override
  public double getPct() {
    return pct;
  }

  @Override
  public boolean isMissing() {
    return missing;
  }
}
