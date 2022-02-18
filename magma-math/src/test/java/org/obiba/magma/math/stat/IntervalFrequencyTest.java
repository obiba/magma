/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math.stat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import org.junit.Test;
import org.obiba.magma.math.summary.IntervalFrequency;
import org.obiba.magma.math.summary.IntervalFrequency.Interval;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class IntervalFrequencyTest {

  @Test
  public void test_sumOfFrequenciesAccountsAllValues() {
    IntervalFrequency freqs = newRandomDistribution(150000);

    long n = 0;
    for(Interval freq : freqs.intervals()) {
      n += freq.getFreq();
    }
    assertThat(n).isEqualTo(150000l);
  }

  @Test
  public void test_density_areaSumsToOne() {
    IntervalFrequency freqs = newRandomDistribution(100);

    // Compute the area of each interval and sum
    BigDecimal totalArea = BigDecimal.ZERO;
    for(Interval freq : freqs.intervals()) {
      BigDecimal width = BigDecimal.valueOf(freq.getUpper()).subtract(BigDecimal.valueOf(freq.getLower()));
      totalArea = totalArea.add(width.multiply(BigDecimal.valueOf(freq.getDensity())));
    }
    // Round the result to 2 decimal places: 0.998 => 1.00
    totalArea = totalArea.setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros();
    assertThat(totalArea).isEqualTo(BigDecimal.ONE);
  }

  @Test
  public void test_add_limitCaseLower() {
    IntervalFrequency freqs = new IntervalFrequency(1.6, 4.6, 10);
    freqs.add(1.6);
  }

  @Test
  public void test_add_limitCaseRoundedLower() {
    // 12345.67 is rounded to 6 significant digits
    IntervalFrequency freqs = new IntervalFrequency(12345.67, 765432.1, 10);
    assertThat(freqs.intervals().first().getLower() < 12345.67).isTrue();
  }

  @Test
  public void test_add_limitCaseUpper() {
    IntervalFrequency freqs = new IntervalFrequency(1.6, 4.6, 10);
    freqs.add(4.6);
  }

  @Test
  public void test_add_limitCaseRoundedUpper() {
    // 12345.67 is rounded to 6 significant digits
    IntervalFrequency freqs = new IntervalFrequency(0, 12345.67, 10);
    assertThat(freqs.intervals().last().getUpper() > 12345.67).isTrue();
  }

  @Test
  public void test_add_limitCaseNoAdd() {
    IntervalFrequency freqs = newRandomDistribution(0);
    long n = 0;
    double density = 0;
    for(Interval freq : freqs.intervals()) {
      n += freq.getFreq();
      density += freq.getDensity();
    }
    assertThat(n).isEqualTo(0l);
    assertThat(density).isEqualTo(0d);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_add_outsideRangeThrowsIAE() {
    IntervalFrequency freqs = new IntervalFrequency(0.0004, 0.175, 10);
    freqs.add(1.5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_lowerEqUpperThrowsIAE() {
    new IntervalFrequency(1, 1, 10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_lowerGtUpperThrowsIAE() {
    new IntervalFrequency(2, 1, 10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_nonPositiveBinCountThrowsIAE() {
    new IntervalFrequency(1, 2, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_negativeBinCountThrowsIAE() {
    new IntervalFrequency(1, 2, -1);
  }

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void test_ctor_integerRounding() {
    IntervalFrequency freqs = new IntervalFrequency(40, 60, 8, true);
    for(Interval freq : freqs.intervals()) {
      double bound = freq.getLower();
      try {
        BigDecimal.valueOf(bound).toBigIntegerExact();
        bound = freq.getUpper();
        BigDecimal.valueOf(bound).toBigIntegerExact();
      } catch(ArithmeticException e) {
        fail("bound is not an exact integer value:" + bound);
      }
    }
  }

  @Test
  public void test_ctor_integerRoundingWillNotMakeIntervalSizeSmallerThanOne() {
    IntervalFrequency freqs = new IntervalFrequency(40, 41, 10, true);
    Interval i = freqs.intervals().first();
    assertThat(i.getUpper() - i.getLower()).isEqualTo(1d);
  }

  @Test
  public void test_toString_returnsAString() {
    IntervalFrequency freqs = newRandomDistribution(1000);
    assertThat(freqs.toString()).isNotNull();
  }

  @Test
  public void test_interval_contains_allCases() {
    IntervalFrequency freqs = newRandomDistribution(1000);
    Interval interval = freqs.intervals().first();

    double min = interval.getLower();
    double lowerThanMin = Math.nextAfter(min, Double.NEGATIVE_INFINITY);
    double higherThanMin = Math.nextAfter(min, Double.POSITIVE_INFINITY);

    double max = interval.getUpper();
    double lowerThanMax = Math.nextAfter(max, Double.NEGATIVE_INFINITY);
    double higherThanMax = Math.nextAfter(max, Double.POSITIVE_INFINITY);

    assertThat(interval.contains(higherThanMin)).isTrue();
    assertThat(interval.contains(lowerThanMin)).isFalse();

    assertThat(interval.contains(lowerThanMax)).isTrue();
    assertThat(interval.contains(higherThanMax)).isFalse();
  }

  @Test
  public void test_interval_equalsAndHashCode() {
    IntervalFrequency first = newRandomDistribution(2, 10, 4, 1000);
    IntervalFrequency second = newRandomDistribution(2, 10, 4, 10000);

    for(Interval interval : first.intervals()) {
      Interval other = second.intervals().tailSet(interval).first();
      // Tests equals
      assertThat(other).isEqualTo(interval);
      // Tests hashCode
      assertThat(other.hashCode()).isEqualTo(interval.hashCode());
    }

    // Other equals tests
    Interval equals = first.intervals().first();
    //noinspection ObjectEqualsNull
    assertThat(equals.equals(null)).isFalse();
    assertThat(equals.equals(equals)).isTrue();
    assertThat(equals.equals(new Object())).isFalse();
  }

  /**
   * Creates a new IntervalFrequency instance with random lower and upper bounds, with random intervals between [1,15]
   *
   * @param observations the number of random observations to add
   * @return
   */
  private IntervalFrequency newRandomDistribution(int observations) {
    Random prng = new Random();
    int pow = prng.nextInt(10) * (prng.nextBoolean() ? -1 : 1);
    double first = prng.nextDouble() * Math.pow(10, pow) * (prng.nextBoolean() ? -1 : 1);
    double second = prng.nextDouble() * Math.pow(10, pow) * (prng.nextBoolean() ? -1 : 1);
    int intervals = Math.abs(prng.nextInt(14)) + 1;
    return newRandomDistribution(Math.min(first, second), Math.max(first, second), intervals, observations);
  }

  private IntervalFrequency newRandomDistribution(double min, double max, int intervals, int observations) {
    return addRandomObservations(new IntervalFrequency(min, max, intervals), min, max, observations);
  }

  private IntervalFrequency addRandomObservations(IntervalFrequency freqs, double min, double max, int observations) {
    Random prng = new Random();
    double diff = max - min;
    for(int i = 0; i < observations; i++) {
      freqs.add(prng.nextDouble() * diff + min);
    }
    return freqs;
  }

}
