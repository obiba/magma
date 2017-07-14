package org.obiba.magma.presto;

import com.facebook.presto.spi.ConnectorHandleResolver;
import com.facebook.presto.spi.connector.Connector;
import com.facebook.presto.spi.connector.ConnectorContext;
import com.facebook.presto.spi.connector.ConnectorFactory;
import org.obiba.magma.Datasource;

import java.util.Map;

public class MagmaConnectorFactory implements ConnectorFactory {

  private final String name;

  private final Datasource datasource;

  public MagmaConnectorFactory(String name, Datasource datasource) {
    this.name = name;
    this.datasource = datasource;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ConnectorHandleResolver getHandleResolver() {
    return new MagmaHandleResolver();
  }

  @Override
  public Connector create(String connectorId, Map<String, String> config, ConnectorContext context) {
    return new MagmaConnector(datasource);
  }
}
