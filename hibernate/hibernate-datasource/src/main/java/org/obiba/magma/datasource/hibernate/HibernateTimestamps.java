package org.obiba.magma.datasource.hibernate;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.datasource.hibernate.HibernateValueTable.HibernateValueSet;
import org.obiba.magma.type.DateTimeType;

public class HibernateTimestamps implements Timestamps {

  private final HibernateValueSet hibernateValueSet;

  public HibernateTimestamps(ValueSet valueSet) {
    this.hibernateValueSet = (HibernateValueSet) valueSet;
  }

  @Override
  public Value getCreated() {
    return DateTimeType.get().valueOf(hibernateValueSet.getValueSetState().getCreated());
  }

  @Override
  public Value getLastUpdate() {
    return DateTimeType.get().valueOf(hibernateValueSet.getValueSetState().getUpdated());
  }

}
