package org.obiba.magma.math.stat;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import org.junit.Test;
import org.obiba.magma.math.stat.IntervalFrequency;
import org.obiba.magma.math.stat.IntervalFrequency.Interval;

public class IntervalFrequencyTest {

  @Test
  public void test_sumOfFrequenciesAccountsAllValues() {
    IntervalFrequency freqs = newRandomDistribution(150000);

    long n = 0;
    for(Interval freq : freqs.intervals()) {
      n += freq.getFreq();
    }
    assertThat(n, is(150000l));
  }

  @Test
  public void test_densityPct_areaSumsToOne() {
    IntervalFrequency freqs = newRandomDistribution(100);

    // Compute the area of each interval and sum
    BigDecimal totalArea = BigDecimal.ZERO;
    for(Interval freq : freqs.intervals()) {
      BigDecimal width = BigDecimal.valueOf(freq.getUpper()).subtract(BigDecimal.valueOf(freq.getLower()));
      totalArea = totalArea.add(width.multiply(BigDecimal.valueOf(freq.getDensityPct())));
    }
    // Round the result to 2 decimal places: 0.998 => 1.00
    totalArea = totalArea.setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros();
    assertThat(totalArea, is(BigDecimal.ONE));
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
    assertThat(freqs.intervals().first().getLower() < 12345.67, is(true));
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
    assertThat(freqs.intervals().last().getUpper() > 12345.67, is(true));
  }

  @Test
  public void test_add_limitCaseNoAdd() {
    IntervalFrequency freqs = newRandomDistribution(0);
    long n = 0;
    double density = 0;
    double densityPct = 0;
    for(Interval freq : freqs.intervals()) {
      n += freq.getFreq();
      density += freq.getDensity();
      densityPct += freq.getDensityPct();
    }
    assertThat(n, is(0l));
    assertThat(density, is(0d));
    assertThat(densityPct, is(0d));
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

  /**
   * Creates a new IntervalFrequency instance with random lower and upper bounds, with random intervals between [1,15]
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
    Random prng = new Random();
    IntervalFrequency freqs = new IntervalFrequency(min, max, intervals);
    double diff = max - min;
    for(int i = 0; i < observations; i++) {
      freqs.add(prng.nextDouble() * diff + min);
    }
    System.out.println(freqs);
    return freqs;
  }

}
