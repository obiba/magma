package org.obiba.magma.datasource.hibernate.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

@Entity
@Table(name = "datasource", uniqueConstraints = { @UniqueConstraint(columnNames = "name") })
public class DatasourceState extends AbstractAttributeAwareEntity implements Timestamp {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String name;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(insertable = true, updatable = false, nullable = false)
  private Date created = new Date();

  @Version
  @Column(nullable = false)
  private Date updated;

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
  public Date getCreated() {
    return new Date(created.getTime());
  }

  @Override
  public Date getUpdated() {
    return new Date(updated.getTime());
  }

}
