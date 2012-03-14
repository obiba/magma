package org.obiba.magma.datasource.limesurvey;

import java.util.Map;

import com.google.common.collect.Maps;

public abstract class LimeLocalizableEntity {

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

  private Map<String, String> localizableLabel = Maps.newHashMap();

  public void addLocalizableAttribute(String key, String value) {
    localizableLabel.put("label:" + key, value);
  }

  public Map<String, String> getLocalizableLabel() {
    return localizableLabel;
  }
}
