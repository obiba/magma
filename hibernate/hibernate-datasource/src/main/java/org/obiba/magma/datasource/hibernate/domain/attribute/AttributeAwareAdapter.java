package org.obiba.magma.datasource.hibernate.domain.attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.obiba.core.domain.AbstractEntity;

@Entity
@Table(name = "attribute_aware")
public class AttributeAwareAdapter extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  private String attributeAwareType;

  @Type(type = "long")
  private Serializable attributeAwareId;

  @OneToMany(cascade = { CascadeType.REMOVE }, mappedBy = "adapter")
  private List<HibernateAttribute> attributes;

  public String getAttributeAwareType() {
    return attributeAwareType;
  }

  public void setAttributeAwareType(String attributeAwareType) {
    this.attributeAwareType = attributeAwareType;
  }

  public Serializable getAttributeAwareId() {
    return attributeAwareId;
  }

  public void setAttributeAwareId(Serializable attributeAwareId) {
    this.attributeAwareId = attributeAwareId;
  }

  public void setAttributeAwareEntity(AbstractAttributeAwareEntity attributeAwareEntity) {
    this.attributeAwareId = attributeAwareEntity.getId();
    this.attributeAwareType = attributeAwareEntity.getAttributeAwareType();
  }

  public List<HibernateAttribute> getAttributes() {
    return (attributes != null ? attributes : new ArrayList<HibernateAttribute>());
  }

  public void setAttributes(List<HibernateAttribute> attributes) {
    this.attributes = new ArrayList<HibernateAttribute>();
    if(attributes != null) {
      this.attributes.addAll(attributes);
    }
  }

}
