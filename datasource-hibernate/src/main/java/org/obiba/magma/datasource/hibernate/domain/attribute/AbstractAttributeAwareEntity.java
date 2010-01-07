package org.obiba.magma.datasource.hibernate.domain.attribute;

import org.obiba.core.domain.AbstractEntity;

public abstract class AbstractAttributeAwareEntity extends AbstractEntity {

  public abstract String getAttributeAwareType();
}
