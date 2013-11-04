package org.obiba.magma.math.stat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.SortedSet;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

/**
 * Computes a frequency distribution of a continuous variable split into constant-sized intervals. Given a lower and
 * upper bound and a number of intervals to create, this class will count the frequency of observations for values
 * within each interval. This can effectively be used for producing histograms.
 */
public class IntervalFrequency implements Serializable {

  private static final long serialVersionUID = 8258609483659122494L;

  // Used for rounding computations to 6 significant digits
  private final static MathContext CTX = new MathContext(6);

  private final SortedSet<Interval> freqTable = Sets.newTreeSet();

  private final BigDecimal min;

  private final BigDecimal max;

  private final BigDecimal intervalSize;

  private long n;

  /**
   * Builds a {@code IntervalFrequency} for values between {@code [lower, upper]} split into {@code intervals} intervals.
   * The flag {@code roundToIntegers} controls whether the bounds of intervals should be rounded to the closest integer
   * value (usually true when input data are integers).
   *
   * @param min the lower bound of all values to count
   * @param max the upper bound of all values to count
   * @param intervals the number of intervals to use, note that the current algorithm may end up using {@code intervals
   * + 1} intervals.
   * @param roundToIntegers when true, interval bounds and size will be rounded (if necessary) to an integer value
   */
  public IntervalFrequency(double min, double max, int intervals, boolean roundToIntegers) {
    if(min >= max) throw new IllegalArgumentException("lower bound must be less than upper bound: " + min + ">" + max);
    if(intervals < 1) throw new IllegalArgumentException("intervals must be positive");

    this.min = maybeRoundToInteger(BigDecimal.valueOf(min), roundToIntegers, RoundingMode.FLOOR)
        .round(new MathContext(6, RoundingMode.FLOOR));
    this.max = maybeRoundToInteger(BigDecimal.valueOf(max), roundToIntegers, RoundingMode.CEILING)
        .round(new MathContext(6, RoundingMode.CEILING));

    // (max - min) / intervals
    BigDecimal is = this.max.subtract(this.min).divide(BigDecimal.valueOf(intervals), CTX);
    is = maybeRoundToInteger(is, roundToIntegers, RoundingMode.HALF_UP);

    // When rounding to integer, we may have rounded the interval size to 0: change it to 1, the smallest interval size
    if(roundToIntegers && is.compareTo(BigDecimal.ZERO) == 0) {
      is = BigDecimal.ONE;
    }

    // Can't seem to find a case that will actually throw the exception. We'll leave the condition anyway since the rest
    // of the code expects non-zero intervalSize value.
    if(is.compareTo(BigDecimal.valueOf(Double.MIN_VALUE)) == 0) {
      throw new ArithmeticException("computed interval size was 0");
    }

    intervalSize = is;

    BigDecimal lower = this.min;
    while(lower.compareTo(this.max) <= 0) {
      BigDecimal upper = lower.add(is);
      freqTable.add(new Interval(lower, upper));
      lower = upper;
    }
  }

  /**
   * Builds a {@code IntervalFrequency} for values between {@code [lower, upper]} split into {@code intervals} intervals.
   *
   * @param min the lower bound of all values to count
   * @param max the upper bound of all values to count
   * @param intervals the number of intervals to use, note that the current algorithm may end up using {@code intervals
   * + 1} intervals.
   */
  public IntervalFrequency(double min, double max, int intervals) {
    this(min, max, intervals, false);
  }

  /**
   * Adds 1 to the frequency of the interval that contains {@code d}.
   *
   * @param d
   */
  public void add(double d) {
    for(Interval interval : freqTable) {
      if(interval.increment(d)) {
        n++;
        return;
      }
    }
    throw new IllegalArgumentException("value is outside [" + min + "," + max + "] bound: " + d);
  }

  /**
   * Returns an unmodifiable view of interval frequency computed by this instance. Note that the iterator will iterate
   * on intervals in order ({@code Interval#compareTo(Interval)})
   *
   * @return an {@code Iterable} over the {@code Interval}
   */
  public SortedSet<Interval> intervals() {
    return ImmutableSortedSet.copyOfSorted(freqTable);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[").append(min).append(",").append(max).append("]/").append(freqTable.size()).append('(')
        .append(intervalSize).append(')').append(" n:").append(n).append('\n');
    for(Interval interval : freqTable) {
      sb.append(interval).append('\n');
    }
    return sb.toString();
  }

  private BigDecimal maybeRoundToInteger(BigDecimal value, boolean roundToInteger, RoundingMode mode) {
    return roundToInteger ? value.setScale(0, mode) : value;
  }

  /**
   * Maintains the frequency of the values between {@code [lower, upper[}
   */
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE_INNER_CLASS")
  public class Interval implements Comparable<Interval>, Serializable {

    private static final long serialVersionUID = -166445440512189129L;

    private final BigDecimal lower;

    private final BigDecimal upper;

    private long freq = 0;

    private Interval(BigDecimal lower, BigDecimal upper) {
      this.lower = lower;
      this.upper = upper;
    }

    public double getLower() {
      return lower.doubleValue();
    }

    public double getUpper() {
      return upper.doubleValue();
    }

    /**
     * Returns the absolute frequency of observations in this interval
     *
     * @return
     */
    public long getFreq() {
      return freq;
    }

    /**
     * Returns the density of this interval (freq / width)
     * <p/>
     * This is the value usually plotted in a histogram, because the surface area of an interval is the frequency of
     * observations.
     *
     * @return
     */
    public double getDensity() {
      return density().doubleValue();
    }

    /**
     * Returns the density percentage of this interval (freq / total / width)
     * <p/>
     * This value represents the proportion of this interval in regards to all others
     *
     * @return
     */
    public double getDensityPct() {
      // freq / width / total
      if(n > 0) {
        return density().divide(BigDecimal.valueOf(n), CTX).doubleValue();
      }
      return 0d;
    }

    /**
     * Returns true when {@code d} is within {@code [lower, upper[}, false otherwise
     *
     * @param d
     * @return
     */
    public boolean contains(double d) {
      BigDecimal dd = BigDecimal.valueOf(d);
      return dd.compareTo(lower) >= 0 && dd.compareTo(upper) < 0;
    }

    @Override
    public int compareTo(Interval o) {
      return (int) Math.signum(lower.doubleValue() - o.lower.doubleValue());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if(obj == null) {
        return false;
      }
      if(this == obj) {
        return true;
      }
      if(obj instanceof Interval) {
        Interval that = (Interval) obj;
        return lower.compareTo(that.lower) == 0 && upper.compareTo(that.upper) == 0;
      }
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      int hashCode = 17;
      hashCode = 37 * hashCode + lower.hashCode();
      hashCode = 37 * hashCode + upper.hashCode();
      return hashCode;
    }

    @Override
    public String toString() {
      return "[" + lower + ',' + upper + "[:" + freq + " (" + density() + ',' + getDensityPct() + ")";
    }

    /**
     * increments the frequency and returns true if {@code d} is within {@code [lower, upper[}. Otherwise returns false
     * and frequency remains unchanged.
     *
     * @param d
     * @return
     */
    boolean increment(double d) {
      boolean contains = contains(d);
      if(contains) freq++;
      return contains;
    }

    /**
     * Computes the density of this interval and rounds the result using {@code CTX}
     *
     * @return
     */
    protected BigDecimal density() {
      // fred / intervalSize, rounded to X significant digits (see CTX)
      return BigDecimal.valueOf(freq / intervalSize.doubleValue()).round(CTX);
    }
  }

}
