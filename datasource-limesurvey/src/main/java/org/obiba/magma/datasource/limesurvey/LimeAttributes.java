package org.obiba.magma.datasource.limesurvey;

import java.util.Arrays;
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

      StringBuilder sb = new StringBuilder(entry.getValue());
      for(String tag : Arrays.asList("script")) {
        int start;
        int end;
        do {
          start = sb.indexOf("<" + tag);
          end = sb.indexOf("</" + tag + ">", start) + tag.length() + 3;
          if(start != -1 && end != -1 && start < end) {
            sb.replace(start, end, " [script] ");
          }
        } while(start != -1 && end != -1 && start < end);
      }
      Attribute.Builder builder = Attribute.Builder.newAttribute(LABEL).withValue(new Locale(entry.getKey()),
          clean(sb.toString()));
      attrs.add(builder.build());
      builder = Attribute.Builder.newAttribute("originalLabel").withValue(new Locale(entry.getKey()),
          entry.getValue());
      attrs.add(builder.build());
    }
    return attrs;
  }

  private String clean(String label) {
    List<String> filter = Arrays.asList("div", "span", "font", "p");
    String copy = label;
    copy = deleteText(copy, "\n");
    copy = deleteText(copy, "\t");
    copy = deleteText(copy, "\r");
    copy = deleteText(copy, "<br />");
    for(String tag : filter) {
      copy = copy.replaceAll("</?" + tag + "[^>]*>", "");
    }
    return copy;
  }

  private String deleteText(String text, String del) {
    return text.replaceAll(del, "");
  }

}
