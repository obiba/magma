package org.obiba.magma.datasource.hibernate.domain.attribute;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.obiba.magma.datasource.hibernate.domain.adaptable.AbstractAdapterEntity;

@Entity
@Table(name = "attribute_aware")
public class AttributeAwareAdapter extends AbstractAdapterEntity {

  private static final long serialVersionUID = 1L;

}
