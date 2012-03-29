package org.obiba.magma.datasource.limesurvey;

import java.util.Map;

import org.obiba.magma.Attribute;

public abstract class LimeLocalizableEntity {

  private LimeAttributes localizableAttributes;

  public LimeLocalizableEntity() {
    localizableAttributes = LimeAttributes.create();
  }

  public LimeLocalizableEntity(String name) {
    this();
    this.name = name;
  }

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Iterable<Attribute> getMagmaAttributes() {
    return localizableAttributes.toMagmaAttributes();
  }

  public void addLocalizableLabelAttribute(String key, String value) {
    localizableAttributes.localizableAttribute(key, value);
  }

  public abstract Map<String, LimeAttributes> getImplicitLabel();
}
