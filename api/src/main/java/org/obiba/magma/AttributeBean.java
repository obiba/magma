package org.obiba.magma;

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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((locale == null) ? 0 : locale.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    AttributeBean other = (AttributeBean) obj;
    if(locale == null) {
      if(other.locale != null) return false;
    } else if(!locale.equals(other.locale)) return false;
    if(name == null) {
      if(other.name != null) return false;
    } else if(!name.equals(other.name)) return false;
    if(value == null) {
      if(other.value != null) return false;
    } else if(!value.equals(other.value)) return false;
    return true;
  }

}
