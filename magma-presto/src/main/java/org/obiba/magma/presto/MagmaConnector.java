package org.obiba.magma.presto;

import com.facebook.presto.spi.connector.Connector;
import com.facebook.presto.spi.connector.ConnectorMetadata;
import com.facebook.presto.spi.connector.ConnectorSplitManager;
import com.facebook.presto.spi.connector.ConnectorTransactionHandle;
import com.facebook.presto.spi.transaction.IsolationLevel;
import org.obiba.magma.Datasource;

public class MagmaConnector implements Connector {

  private final Datasource datasource;

  public MagmaConnector(Datasource datasource) {
    this.datasource = datasource;
  }

  @Override
  public ConnectorTransactionHandle beginTransaction(IsolationLevel isolationLevel, boolean readOnly) {
    return MagmaTransactionHandle.INSTANCE;
  }

  @Override
  public ConnectorMetadata getMetadata(ConnectorTransactionHandle transactionHandle) {
    return new MagmaMetadata(datasource);
  }

  @Override
  public ConnectorSplitManager getSplitManager() {
    return new MagmaSplitManager();
  }
}
