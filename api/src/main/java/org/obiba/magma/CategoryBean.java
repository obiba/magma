package org.obiba.meta;

class CategoryBean extends AbstractAttributeAware implements Category {

  String name;

  String code;

  CategoryBean(String name, String code) {
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

}
