package org.obiba.magma.datasource.hibernate.domain.adaptable;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.obiba.core.domain.AbstractEntity;

@MappedSuperclass
public abstract class AbstractAdapterEntity extends AbstractEntity {

  private String adaptableType;

  @Type(type = "long")
  private Serializable adaptableId;

  public String getAdaptableType() {
    return adaptableType;
  }

  public void setAdaptableType(String adaptableType) {
    this.adaptableType = adaptableType;
  }

  public Serializable getAdaptableId() {
    return adaptableId;
  }

  public void setAdaptableId(Serializable adaptableId) {
    this.adaptableId = adaptableId;
  }

  public void setAdaptable(AbstractAdaptableEntity adaptable) {
    this.adaptableId = adaptable.getId();
    this.adaptableType = adaptable.getAdaptableType();
  }

  @Override
  public String toString() {
    return adaptableType + "[" + adaptableId + "]";
  }
}
