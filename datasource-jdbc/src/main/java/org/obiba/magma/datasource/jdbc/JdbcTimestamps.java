package org.obiba.magma.datasource.jdbc;

import javax.annotation.Nonnull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.type.DateTimeType;

public class JdbcTimestamps implements Timestamps {

  private final JdbcValueSet valueSet;

  public JdbcTimestamps(ValueSet valueSet) {
    this.valueSet = (JdbcValueSet) valueSet;
  }

  @Nonnull
  @Override
  public Value getCreated() {
    return valueSet.getValueTable().hasCreatedTimestampColumn() //
        ? valueSet.getCreated() //
        : DateTimeType.get().nullValue();
  }

  @Nonnull
  @Override
  public Value getLastUpdate() {
    return valueSet.getValueTable().hasUpdatedTimestampColumn() //
        ? valueSet.getUpdated() //
        : DateTimeType.get().nullValue();
  }
}
