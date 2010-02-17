package org.obiba.magma.datasource.hibernate.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.obiba.magma.datasource.hibernate.domain.attribute.AbstractAttributeAwareEntity;

@Entity
@Table(name = "category")
public class CategoryState extends AbstractAttributeAwareEntity {

  private static final long serialVersionUID = 1L;

  private String name;

  private String code;

  private Boolean missing;

  private int categoryIndex;

  public CategoryState() {

  }

  public CategoryState(String name, String code, Boolean missing) {
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
    return missing != null ? missing : false;
  }

  public int getCategoryIndex() {
    return categoryIndex;
  }

  public void setCategoryIndex(int categoryIndex) {
    this.categoryIndex = categoryIndex;
  }

}
