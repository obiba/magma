package org.obiba.magma.presto;

import com.facebook.presto.spi.Plugin;
import com.facebook.presto.spi.connector.ConnectorFactory;
import com.google.common.collect.ImmutableList;
import org.obiba.magma.Datasource;

public class MagmaPlugin implements Plugin {

  private final String name;

  private final Datasource datasource;

  public MagmaPlugin(String name, Datasource datasource) {
    this.name = name;
    this.datasource = datasource;
  }


  @Override
  public Iterable<ConnectorFactory> getConnectorFactories() {
    return ImmutableList.of(new MagmaConnectorFactory(name, datasource));
  }
}
