package org.obiba.magma.datasource.jpa.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.VariableEntity;

@Entity
@Table(name = "variable_entity")
public class VariableEntityState extends AbstractEntity implements VariableEntity {

  private static final long serialVersionUID = 1L;

  private String identifier;

  private String type;

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

}
