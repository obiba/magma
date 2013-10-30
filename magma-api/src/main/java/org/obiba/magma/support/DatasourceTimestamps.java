package org.obiba.magma.support;

import javax.annotation.Nonnull;

import org.obiba.magma.Datasource;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.type.DateTimeType;

public class DatasourceTimestamps implements Timestamps {

  private Value created;

  private Value lastUpdate;

  public DatasourceTimestamps(Datasource datasource) {
    created = DateTimeType.get().now();
    lastUpdate = null;
    for(ValueTable table : datasource.getValueTables()) {
      Timestamps ts = table.getTimestamps();
      if(created.compareTo(ts.getCreated()) > 0) {
        created = ts.getCreated();
      }
      if(lastUpdate == null || lastUpdate.compareTo(ts.getLastUpdate()) < 0) {
        lastUpdate = ts.getLastUpdate();
      }
    }
    if(lastUpdate == null) {
      lastUpdate = created;
    }
  }

  @Nonnull
  @Override
  public Value getLastUpdate() {
    return lastUpdate;
  }

  @Nonnull
  @Override
  public Value getCreated() {
    return created;
  }
}