/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UnionTimestamps implements Timestamps {

  private final Iterable<Timestamps> timestamps;

  public UnionTimestamps(Iterable<? extends Timestamped> timestampeds) {
    timestamps = Iterables.transform(timestampeds, Timestamped.ToTimestamps);
  }

  public UnionTimestamps(Collection<Timestamps> timestamps) {
    this.timestamps = timestamps;
  }

  @NotNull
  @Override
  public Value getCreated() {
    return getTimestamp(ExtractCreatedFunction.INSTANCE, true);
  }

  @NotNull
  @Override
  public Value getLastUpdate() {
    return getTimestamp(ExtractLastUpdateFunction.INSTANCE, false);
  }

  /**
   * Extracts all the timestamp values, sorts them and returns either the earliest value or the latest value depending
   * on the {@code earliest} argument.
   *
   * @param extractTimestampFunction the function used to extract the timestamp to work with (either created or
   *                                 lastUpdate)
   * @param earliest                 whether to return the earliest value or the latest value
   * @return the earliest/latest timestamp from the set of timestamps. This method never returns null.
   */
  private Value getTimestamp(Function<Timestamps, Value> extractTimestampFunction, boolean earliest) {
    Iterable<Value> created = Iterables.transform(timestamps, extractTimestampFunction);
    Value[] values = Iterables.toArray(getNonNullValues(created), Value.class);
    if (values.length > 0) {
      Arrays.sort(values);
      return values[earliest ? 0 : values.length - 1];
    } else {
      return DateTimeType.get().nullValue();
    }
  }

  /**
   * Filters Value instances that are null or that isNull() returns true out of {@code values}.
   *
   * @param values
   * @return
   */
  private Iterable<Value> getNonNullValues(Iterable<Value> values) {
    return StreamSupport.stream(values.spliterator(), false) //
        .filter(value -> value != null && !value.isNull()) //
        .collect(Collectors.toList());
  }

  private static final class ExtractLastUpdateFunction implements Function<Timestamps, Value> {

    @SuppressWarnings("TypeMayBeWeakened")
    private static final ExtractLastUpdateFunction INSTANCE = new ExtractLastUpdateFunction();

    @Override
    public Value apply(Timestamps from) {
      return from == null ? null : from.getLastUpdate();
    }

  }

  private static final class ExtractCreatedFunction implements Function<Timestamps, Value> {

    @SuppressWarnings("TypeMayBeWeakened")
    private static final ExtractCreatedFunction INSTANCE = new ExtractCreatedFunction();

    @Override
    public Value apply(Timestamps from) {
      return from == null ? null : from.getCreated();
    }
  }
}
