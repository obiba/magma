package org.obiba.magma.datasource.hibernate.domain.attribute;

import org.obiba.core.domain.AbstractEntity;

@SuppressWarnings("serial")
public abstract class AbstractAttributeAwareEntity extends AbstractEntity {

  public abstract String getAttributeAwareType();
}
