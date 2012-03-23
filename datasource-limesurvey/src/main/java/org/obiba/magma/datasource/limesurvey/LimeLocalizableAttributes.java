package org.obiba.magma.datasource.limesurvey;

import java.util.Map;

import com.google.common.collect.Maps;

public class LimeLocalizableAttributes {

  public static final String LABEL = "label:";

  private Map<String, String> attributes = Maps.newHashMap();

  private LimeLocalizableAttributes() {
  }

  public static LimeLocalizableAttributes create() {
    return new LimeLocalizableAttributes();
  }

  public LimeLocalizableAttributes localizableAttribute(String key, String value) {
    attributes.put(LABEL + key, value);
    return this;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

}
