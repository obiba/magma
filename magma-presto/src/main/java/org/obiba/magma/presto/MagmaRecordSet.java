package org.obiba.magma.presto;

import com.facebook.presto.spi.RecordCursor;
import com.facebook.presto.spi.RecordSet;
import com.facebook.presto.spi.type.Type;

import java.util.List;

public class MagmaRecordSet implements RecordSet {

  @Override
  public List<Type> getColumnTypes() {
    return null;
  }

  @Override
  public RecordCursor cursor() {
    return new MagmaRecordCursor();
  }
}
