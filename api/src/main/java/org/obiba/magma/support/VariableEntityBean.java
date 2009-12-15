package org.obiba.magma.support;

import org.obiba.magma.VariableEntity;

public class VariableEntityBean implements VariableEntity {

  private String entityType;

  private String entityIdentifier;

  private transient volatile int hashCode = 0;

  public VariableEntityBean(String entityType, String entityIdentifier) {
    if(entityIdentifier != null && entityIdentifier.trim().length() == 0) {
      throw new IllegalArgumentException("Empty identifier for entity type " + entityType + " (identifier was [" + entityIdentifier + "])");
    }

    this.entityType = entityType;
    this.entityIdentifier = entityIdentifier;
  }

  @Override
  public String getIdentifier() {
    return entityIdentifier;
  }

  @Override
  public String getType() {
    return entityType;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj instanceof VariableEntity) {
      VariableEntity rhs = (VariableEntity) obj;
      return entityType.equals(rhs.getType()) && entityIdentifier.equals(rhs.getIdentifier());
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {// Lazily initialized, cached hashCode
    if(hashCode == 0) {
      int result = 17;
      result = 37 * result + entityType.hashCode();
      result = 37 * result + entityIdentifier.hashCode();
      hashCode = result;
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return "entity[" + getType() + ":" + getIdentifier() + "]";
  }
}
