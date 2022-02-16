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

import com.google.common.collect.Lists;
import org.obiba.magma.math.FrequenciesSummary;
import org.obiba.magma.math.Frequency;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DefaultFrequenciesSummary implements FrequenciesSummary {

  private long n = 0;

  private List<Frequency> frequencies = Lists.newArrayList();

  @Override
  public long getN() {
    return n;
  }

  public void setN(long n) {
    this.n = n;
  }

  @Override
  public Iterable<Frequency> getFrequencies() {
    return frequencies;
  }

  public void addFrequency(Frequency freq) {
    frequencies.add(freq);
  }

  /**
   * Sort by most frequent first.
   */
  public void sortFrequencies() {
    Collections.sort(frequencies, new Comparator<Frequency>() {
      @Override
      public int compare(Frequency o1, Frequency o2) {
        return (int) (o2.getFreq() - o1.getFreq());
      }
    });
  }
}
