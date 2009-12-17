package org.obiba.magma.datasource.jpa.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.obiba.core.domain.AbstractEntity;

@Entity
@Table(name = "value_table")
public class ValueTableState extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  private String name;

  private String entityType;

  private Integer pos;

  @ManyToOne(optional = false)
  @JoinColumn(name = "datasource_id")
  private DatasourceState datasource;

  public ValueTableState() {
    super();
  }

  public ValueTableState(String name, String entityType, DatasourceState datasource) {
    super();
    this.name = name;
    this.entityType = entityType;
    this.datasource = datasource;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getName() {
    return name;
  }

  public DatasourceState getDatasource() {
    return datasource;
  }

}
