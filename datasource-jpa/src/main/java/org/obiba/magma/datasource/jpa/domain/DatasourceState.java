package org.obiba.magma.datasource.jpa.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.obiba.magma.datasource.jpa.domain.adaptable.AbstractAdaptableEntity;

@Entity
@Table(name = "datasource")
public class DatasourceState extends AbstractAdaptableEntity {

  private static final long serialVersionUID = 1L;

  private String name;

  private String type;

  public DatasourceState() {
    super();
  }

  public DatasourceState(String name, String type) {
    super();
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  @Override
  public String getAdaptableType() {
    return "datasource";
  }

}
