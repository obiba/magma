package org.obiba.magma.datasource.hibernate.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.magma.VariableEntity;

@Entity
@Table(name = "variable_entity", uniqueConstraints = { @UniqueConstraint(columnNames = { "type", "identifier" }) })
public class VariableEntityState extends AbstractTimestampedEntity implements VariableEntity {

  private static final long serialVersionUID = 1L;

  private String identifier;

  private String type;

  private transient volatile int hashCode = 0;

  public VariableEntityState() {
    super();
  }

  public VariableEntityState(String identifier, String type) {
    super();
    this.identifier = identifier;
    this.type = type;
  }

  @Override
  public String getIdentifier() {
    return identifier;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj instanceof VariableEntity) {
      VariableEntity rhs = (VariableEntity) obj;
      return type.equals(rhs.getType()) && identifier.equals(rhs.getIdentifier());
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {// Lazily initialized, cached hashCode
    if(hashCode == 0) {
      int result = 17;
      result = 37 * result + type.hashCode();
      result = 37 * result + identifier.hashCode();
      hashCode = result;
    }
    return hashCode;
  }

  @Override
  public int compareTo(VariableEntity o) {
    int compare = type.compareTo(o.getType());
    return compare != 0 ? compare : identifier.compareTo(o.getIdentifier());
  }

}
