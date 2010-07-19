package org.obiba.magma.views;

import java.util.Arrays;
import java.util.List;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class JoinTimestamps implements Timestamps {

  private final Iterable<Timestamps> timestamps;

  public JoinTimestamps(final ValueSet valueSet, List<ValueTable> tables) {
    this.timestamps = Iterables.transform(getValueTablesWithNonNullTimestamps(valueSet, tables), new Function<ValueTable, Timestamps>() {

      @Override
      public Timestamps apply(ValueTable table) {
        return table.getTimestamps(valueSet);
      }

    });
  }

  private Iterable<ValueTable> getValueTablesWithNonNullTimestamps(final ValueSet valueSet, Iterable<ValueTable> valueTables) {
    return Iterables.filter(valueTables, new Predicate<ValueTable>() {

      @Override
      public boolean apply(ValueTable valueTable) {
        return valueTable.getTimestamps(valueSet) == null ? false : true;
      }
    });
  }

  @Override
  public Value getCreated() {
    Iterable<Value> created = Iterables.transform(timestamps, new Function<Timestamps, Value>() {

      @Override
      public Value apply(Timestamps timestamp) {
        return timestamp.getCreated();
      }

    });
    Value[] values = Iterables.toArray(getNonNullValues(created), Value.class);
    if(values.length > 0) {
      Arrays.sort(values);
      return values[0];
    } else {
      return null;
    }
  }

  private Iterable<Value> getNonNullValues(Iterable<Value> values) {
    return Iterables.filter(values, new Predicate<Value>() {

      @Override
      public boolean apply(Value value) {
        return value == null ? false : true;
      }

    });

  }

  @Override
  public Value getLastUpdate() {
    Iterable<Value> created = Iterables.transform(timestamps, new Function<Timestamps, Value>() {

      @Override
      public Value apply(Timestamps timestamp) {
        return timestamp.getLastUpdate();
      }

    });
    Value[] values = Iterables.toArray(getNonNullValues(created), Value.class);
    if(values.length > 0) {
      Arrays.sort(values);
      return values[values.length - 1];
    } else {
      return null;
    }
  }

}
