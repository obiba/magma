package org.obiba.magma.datasource.hibernate;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.datasource.hibernate.HibernateValueTable.HibernateValueSet;
import org.obiba.magma.type.DateTimeType;

public class HibernateTimestamps implements Timestamps {

  private final Value created;

  private final Value updated;

  public HibernateTimestamps(ValueSet valueSet) {
    HibernateValueSet hibernateValueSet = (HibernateValueSet) valueSet;
    this.created = DateTimeType.get().valueOf(hibernateValueSet.getValueSetState().getCreated());
    this.updated = DateTimeType.get().valueOf(hibernateValueSet.getValueSetState().getUpdated());
  }

  @Override
  public Value getCreated() {
    return created;
  }

  @Override
  public Value getLastUpdate() {
    return updated;
  }

}
