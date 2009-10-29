package org.obiba.meta.support;

import org.obiba.meta.ValueSetReference;
import org.obiba.meta.VariableEntity;

public class ValueSetReferenceBean implements ValueSetReference {

  private String entityType;

  private String entityIdentifier;

  private String valueSetIdentifier;

  private VariableEntityBean entityBean = new VariableEntityBean();

  public ValueSetReferenceBean(String entityType, String entityIdentifier, String valueSetIdentifier) {
    this.entityType = entityType;
    this.entityIdentifier = entityIdentifier;
    this.valueSetIdentifier = valueSetIdentifier;
  }

  @Override
  public String getIdentifier() {
    return valueSetIdentifier;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return entityBean;
  }

  @Override
  public String toString() {
    return "ValueSetReference[" + getVariableEntity() + ":" + getIdentifier() + "]";
  }

  private class VariableEntityBean implements VariableEntity {

    @Override
    public String getIdentifier() {
      return entityIdentifier;
    }

    @Override
    public String getType() {
      return entityType;
    }

    @Override
    public String toString() {
      return "entity[" + getType() + ":" + getIdentifier() + "]";
    }
  }
}
