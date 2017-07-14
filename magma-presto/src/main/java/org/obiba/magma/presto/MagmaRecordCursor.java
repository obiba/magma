package org.obiba.magma.presto;

import com.facebook.presto.spi.RecordCursor;
import com.facebook.presto.spi.type.Type;
import io.airlift.slice.Slice;

public class MagmaRecordCursor implements RecordCursor {
  @Override
  public long getTotalBytes() {
    return 0;
  }

  @Override
  public long getCompletedBytes() {
    return 0;
  }

  @Override
  public long getReadTimeNanos() {
    return 0;
  }

  @Override
  public Type getType(int field) {
    return null;
  }

  @Override
  public boolean advanceNextPosition() {
    return false;
  }

  @Override
  public boolean getBoolean(int field) {
    return false;
  }

  @Override
  public long getLong(int field) {
    return 0;
  }

  @Override
  public double getDouble(int field) {
    return 0;
  }

  @Override
  public Slice getSlice(int field) {
    return null;
  }

  @Override
  public Object getObject(int field) {
    return null;
  }

  @Override
  public boolean isNull(int field) {
    return false;
  }

  @Override
  public void close() {

  }
}
