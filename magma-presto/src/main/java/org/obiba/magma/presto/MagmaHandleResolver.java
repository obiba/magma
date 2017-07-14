package org.obiba.magma.presto;

import com.facebook.presto.spi.*;

public class MagmaHandleResolver implements ConnectorHandleResolver {
  @Override
  public Class<? extends ConnectorTableHandle> getTableHandleClass() {
    return MagmaTableHandle.class;
  }

  @Override
  public Class<? extends ConnectorTableLayoutHandle> getTableLayoutHandleClass() {
    return MagmaTableLayoutHandle.class;
  }

  @Override
  public Class<? extends ColumnHandle> getColumnHandleClass() {
    return MagmaColumnHandle.class;
  }

  @Override
  public Class<? extends ConnectorSplit> getSplitClass() {
    return MagmaSplit.class;
  }
}
