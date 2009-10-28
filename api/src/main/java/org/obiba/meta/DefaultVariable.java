package org.obiba.meta;

class DefaultVariable implements Variable {

  String name;

  String entityType;

  String mimeType;

  String unit;

  ValueType valueType;

  String referencedEntityType;

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
    return mimeType;
  }

  @Override
  public String getUnit() {
    return unit;
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

  @Override
  public String getReferencedEntityType() {
    return referencedEntityType;
  }

}
