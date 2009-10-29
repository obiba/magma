package org.obiba.meta;

import javax.xml.namespace.QName;

class DefaultVariable implements Variable {

  String collection;

  String name;

  String entityType;

  String mimeType;

  String unit;

  ValueType valueType;

  String referencedEntityType;

  @Override
  public QName getQName() {
    return new QName(getCollection(), getName());
  }

  @Override
  public String getCollection() {
    return collection;
  }

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
