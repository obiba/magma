package org.obiba.meta;

import java.util.Locale;

class AttributeBean implements Attribute {

  String name;

  Locale locale;

  Value value;

  AttributeBean(String name) {
    this.name = name;
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isLocalised() {
    return locale != null;
  }

  @Override
  public Value getValue() {
    return value;
  }

  @Override
  public ValueType getValueType() {
    return value.getValueType();
  }

}
