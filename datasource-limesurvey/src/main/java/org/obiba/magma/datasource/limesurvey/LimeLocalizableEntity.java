package org.obiba.magma.datasource.limesurvey;

import java.util.Map;

public abstract class LimeLocalizableEntity {

  private LimeAttributes localizableAttributes = LimeAttributes.create();

  public LimeLocalizableEntity() {
  }

  public LimeLocalizableEntity(String name) {
    this.name = name;
  }

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void addLocalizableAttribute(String key, String value) {
    localizableAttributes.localizableAttribute(key, value);
  }

  public Map<String, String> getLocalizableAttributes() {
    return localizableAttributes.getAttributes();
  }

  public abstract Map<String, LimeAttributes> getImplicitLabel();
}
