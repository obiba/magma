package org.obiba.magma.datasource.limesurvey;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.Attribute;

public class LimeAttributes {

  public static final String LABEL = "label";

  private Map<String, String> attributes;

  private Map<String, String> localizableLabelAttributes;

  private LimeAttributes() {
    attributes = Maps.newHashMap();
    localizableLabelAttributes = Maps.newHashMap();
  }

  public static LimeAttributes create() {
    return new LimeAttributes();
  }

  public LimeAttributes localizableAttribute(String key, String value) {
    localizableLabelAttributes.put(key, value);
    return this;
  }

  public LimeAttributes attribute(String key, String value) {
    attributes.put(key, value);
    return this;
  }

  public Iterable<Attribute> toMagmaAttributes() {
    List<Attribute> attrs = Lists.newArrayList();
    for(Map.Entry<String, String> entry : attributes.entrySet()) {
      Attribute.Builder builder = Attribute.Builder.newAttribute(entry.getKey()).withValue(entry.getValue());
      attrs.add(builder.build());
    }
    for(Map.Entry<String, String> entry : localizableLabelAttributes.entrySet()) {
      Attribute.Builder builder = Attribute.Builder.newAttribute(LABEL).withValue(new Locale(entry.getKey()),
          entry.getValue());
      attrs.add(builder.build());
    }
    return attrs;
  }

}
