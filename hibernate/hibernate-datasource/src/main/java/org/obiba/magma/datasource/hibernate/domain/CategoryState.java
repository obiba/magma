package org.obiba.magma.datasource.hibernate.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
@Table(name = "category")
public class CategoryState extends AbstractAttributeAwareEntity implements Timestamp {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String name;

  private String code;

  @Column(nullable = false)
  private boolean missing;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(insertable = true, updatable = false, nullable = false)
  private Date created = new Date();

  @Version
  @Column(nullable = false)
  private Date updated;

  public CategoryState() {

  }

  public CategoryState(String name, String code, boolean missing) {
    super();
    this.name = name;
    this.code = code;
    this.missing = missing;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setMissing(Boolean missing) {
    this.missing = missing;
  }

  public String getName() {
    return name;
  }

  public boolean isMissing() {
    return missing;
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
