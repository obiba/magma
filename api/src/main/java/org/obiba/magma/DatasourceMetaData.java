package org.obiba.magma;

import java.util.Map;

import com.google.common.collect.Maps;

public class DatasourceMetaData {

  private String version;

  private Map<String, String> metadata = Maps.newHashMap();

  public DatasourceMetaData(String version) {
    this.version = version;
  }

  public String getVersion() {
    return version;
  }

  public String getValue(String key) {
    return metadata.get(key);
  }

  public DatasourceMetaData putValue(String key, String value) {
    metadata.put(key, value);
    return this;
  }

}
