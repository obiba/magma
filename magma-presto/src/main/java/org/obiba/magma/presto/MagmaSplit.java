package org.obiba.magma.presto;

import com.facebook.presto.spi.ConnectorSplit;
import com.facebook.presto.spi.HostAddress;

import java.util.List;

public class MagmaSplit implements ConnectorSplit {
  @Override
  public boolean isRemotelyAccessible() {
    return false;
  }

  @Override
  public List<HostAddress> getAddresses() {
    return null;
  }

  @Override
  public Object getInfo() {
    return this;
  }
}
