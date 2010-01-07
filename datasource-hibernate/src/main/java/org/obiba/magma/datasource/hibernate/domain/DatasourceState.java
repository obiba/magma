package org.obiba.magma.datasource.hibernate.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.magma.datasource.hibernate.domain.attribute.AbstractAttributeAwareEntity;

@Entity
@Table(name = "datasource", uniqueConstraints = { @UniqueConstraint(columnNames = "name") })
public class DatasourceState extends AbstractAttributeAwareEntity {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
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
  public String getAttributeAwareType() {
    return "datasource";
  }

}
