package org.obiba.magma.support;

import javax.annotation.Nonnull;

import org.obiba.magma.VariableEntity;

public class VariableEntityBean implements VariableEntity {

  @Nonnull
  private final String entityType;

  @Nonnull
  private final String entityIdentifier;

  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient volatile int hashCode = 0;

  @SuppressWarnings("ConstantConditions")
  public VariableEntityBean(@Nonnull String entityType, @Nonnull String entityIdentifier) {
    if(entityType == null) throw new IllegalArgumentException("entityType cannot be null");
    if(entityIdentifier == null) throw new IllegalArgumentException("entityIdentifier cannot be null");
    if(entityIdentifier.trim().isEmpty()) throw new IllegalArgumentException("entityIdentifier cannot be empty");

    this.entityType = entityType;
    this.entityIdentifier = entityIdentifier;
  }

  @Override
  @Nonnull
  public String getIdentifier() {
    return entityIdentifier;
  }

  @Override
  @Nonnull
  public String getType() {
    return entityType;
  }

  @Override
  public int compareTo(VariableEntity that) {
    if(that == null) {
      throw new IllegalArgumentException();
    }
    int compare = entityType.compareTo(that.getType());
    return compare == 0 ? entityIdentifier.compareTo(that.getIdentifier()) : compare;
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
