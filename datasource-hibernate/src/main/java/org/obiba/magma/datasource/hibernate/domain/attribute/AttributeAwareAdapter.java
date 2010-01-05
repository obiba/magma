package org.obiba.magma.datasource.hibernate.domain.attribute;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.obiba.magma.datasource.hibernate.domain.adaptable.AbstractAdapterEntity;

@Entity
@Table(name = "attribute_aware")
public class AttributeAwareAdapter extends AbstractAdapterEntity {

  private static final long serialVersionUID = 1L;

  @OneToMany(cascade = { CascadeType.REMOVE }, mappedBy = "adapter")
  private List<HibernateAttribute> attributes;

  public List<HibernateAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<HibernateAttribute> attributes) {
    this.attributes = attributes;
  }

}
