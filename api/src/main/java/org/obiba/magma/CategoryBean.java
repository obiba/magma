package org.obiba.magma;

class CategoryBean extends AbstractAttributeAware implements Category {

  String name;

  String code;

  boolean missing;

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

  @Override
  public boolean isMissing() {
    return missing;
  }

}
