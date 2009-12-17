package org.obiba.magma.datasource.jpa.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.magma.datasource.jpa.domain.adaptable.AbstractAdaptableEntity;

@Entity
@Table(name = "datasource", uniqueConstraints = { @UniqueConstraint(columnNames = "name") })
public class DatasourceState extends AbstractAdaptableEntity {

  private static final long serialVersionUID = 1L;

  private String name;

  public DatasourceState() {
    super();
  }

  public DatasourceState(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String getAdaptableType() {
    return "datasource";
  }

}
