package org.obiba.magma.datasource.jdbc;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;

public class JdbcTimestamps implements Timestamps {

  private final JdbcValueSet valueSet;

  public JdbcTimestamps(ValueSet valueSet) {
    this.valueSet = (JdbcValueSet) valueSet;
  }

  @Override
  public Value getCreated() {
    if(valueSet.getValueTable().getDatasource().getSettings().isCreatedTimestampColumnNameProvided()) {
      return valueSet.getCreated();
    } else {
      return null;
    }
  }

  @Override
  public Value getLastUpdate() {
    if(valueSet.getValueTable().getDatasource().getSettings().isUpdatedTimestampColumnNameProvided()) {
      return valueSet.getUpdated();
    } else {
      return null;
    }
  }

}
