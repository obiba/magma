package org.obiba.meta;

class CategoryBean implements Category {

  private Variable variable;

  private String name;

  private String code;

  CategoryBean(Variable variable, String name, String code) {
    this.variable = variable;
    this.name = name;
    this.code = code;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

}
