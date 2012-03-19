package org.obiba.magma.datasource.limesurvey;

import java.util.Map;

import com.google.common.collect.Maps;

public class LimeLocalizableAttributes {

  private Map<String, String> attributes = Maps.newHashMap();

  private LimeLocalizableAttributes() {
  }

  public static LimeLocalizableAttributes create() {
    return new LimeLocalizableAttributes();
  }

  public LimeLocalizableAttributes localizableAttribute(String key, String value) {
    attributes.put("label:" + key, value);
    return this;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

}
