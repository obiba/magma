package org.obiba.magma.datasource.hibernate.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.obiba.magma.datasource.hibernate.domain.attribute.AbstractAttributeAwareEntity;

@Entity
@Table(name = "category")
public class CategoryState extends AbstractAttributeAwareEntity {

  private static final long serialVersionUID = 1L;

  private String name;

  private String code;

  private Boolean missing;

  @ManyToOne(optional = false)
  @JoinColumn(name = "variable_id")
  private VariableState variable;

  private Integer pos;

  public CategoryState() {

  }

  public CategoryState(VariableState variable, String name, String code, Boolean missing) {
    super();
    this.variable = variable;
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

  public VariableState getVariable() {
    return variable;
  }

  public Integer getPosition() {
    return pos;
  }

  public void setPosition(Integer pos) {
    this.pos = pos;
  }

  @Override
  public String getAttributeAwareType() {
    return "category";
  }

}
