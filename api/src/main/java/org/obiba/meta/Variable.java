package org.obiba.meta;

class Variable implements IVariable {

  String name;

  String entityType;

  String mimeType;

  String unit;

  ValueType valueType;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  @Override
  public String getMimeType() {
    return null;
  }

  @Override
  public String getUnit() {
    return null;
  }

  @Override
  public ValueType getValueType() {
    return valueType;
  }

  @Override
  public String getAttribute(String name) {
    // TODO: Implement attributes
    return null;
  }

}
