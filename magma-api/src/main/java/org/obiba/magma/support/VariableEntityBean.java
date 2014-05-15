package org.obiba.magma.support;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.obiba.magma.VariableEntity;

import com.google.common.collect.ComparisonChain;

public class VariableEntityBean implements VariableEntity, Serializable {

  private static final long serialVersionUID = 345393053905353342L;

  @NotNull
  private final String entityType;

  @NotNull
  private final String entityIdentifier;

  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient volatile int hashCode = 0;

  @SuppressWarnings("ConstantConditions")
  public VariableEntityBean(@NotNull String entityType, @NotNull String entityIdentifier) {
    if(entityType == null) throw new IllegalArgumentException("entityType cannot be null");
    if(entityIdentifier == null) throw new IllegalArgumentException("entityIdentifier cannot be null");
    if(entityIdentifier.trim().isEmpty()) throw new IllegalArgumentException("entityIdentifier cannot be empty");

    this.entityType = entityType;
    this.entityIdentifier = entityIdentifier;
  }

  @Override
  @NotNull
  public String getIdentifier() {
    return entityIdentifier;
  }

  @Override
  @NotNull
  public String getType() {
    return entityType;
  }

  @Override
  public int compareTo(VariableEntity that) {
    return ComparisonChain.start() //
        .compare(entityType, that.getType()) //
        .compare(entityIdentifier, that.getIdentifier()) //
        .result();
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
