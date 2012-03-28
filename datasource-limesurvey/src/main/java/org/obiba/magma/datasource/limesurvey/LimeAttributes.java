package org.obiba.magma.datasource.limesurvey;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.Attribute;

public class LimeAttributes {

  public static final String LABEL = "label:";

  private Map<String, String> attributes;

  private LimeAttributes() {
    attributes = Maps.newHashMap();
  }

  public static LimeAttributes create() {
    return new LimeAttributes();
  }

  public LimeAttributes localizableAttribute(String key, String value) {
    return attribute(LABEL + key, value);
  }

  public LimeAttributes attribute(String key, String value) {
    attributes.put(key, value);
    return this;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public Iterable<Attribute> toMagmaAttributes() {
    List<Attribute> attrs = Lists.newArrayList();
    for(Map.Entry<String, String> entry : attributes.entrySet()) {
      Attribute.Builder builder = Attribute.Builder.newAttribute(entry.getKey()).withValue(entry.getValue());
      attrs.add(builder.build());
    }
    return attrs;
  }

}
