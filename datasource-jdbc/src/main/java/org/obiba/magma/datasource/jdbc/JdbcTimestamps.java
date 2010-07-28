package org.obiba.magma.datasource.jdbc;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.type.DateTimeType;

public class JdbcTimestamps implements Timestamps {

  private final JdbcValueSet valueSet;

  public JdbcTimestamps(ValueSet valueSet) {
    this.valueSet = (JdbcValueSet) valueSet;
  }

  @Override
  public Value getCreated() {
    if(valueSet.getValueTable().hasCreatedTimestampColumn()) {
      return valueSet.getCreated();
    } else {
      return DateTimeType.get().nullValue();
    }
  }

  @Override
  public Value getLastUpdate() {
    if(valueSet.getValueTable().hasUpdatedTimestampColumn()) {
      return valueSet.getUpdated();
    } else {
      return DateTimeType.get().nullValue();
    }
  }
}
