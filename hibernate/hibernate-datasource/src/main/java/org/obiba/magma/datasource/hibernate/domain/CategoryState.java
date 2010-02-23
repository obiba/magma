package org.obiba.magma.datasource.hibernate.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "category")
public class CategoryState extends AbstractAttributeAwareEntity implements Timestamped {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String name;

  private String code;

  @Column(nullable = false)
  private boolean missing;

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

}
