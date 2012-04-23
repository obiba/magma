package org.obiba.magma.datasource.limesurvey;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Attribute;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LimeAttributes {

  private static final String LIMESURVEY_NAMESPACE = "limesurvey";
  
  // Attributes that should not be part of the limesurvey namespace
  private static final Set<String> OPAL_ATTRIBUTES = ImmutableSet.of("label");

  private Map<String, String> attributes;

  private LimeAttributes() {
    attributes = Maps.newHashMap();
  }

  public static LimeAttributes create() {
    return new LimeAttributes();
  }

  public LimeAttributes attribute(String key, String value) {
    attributes.put(key, value);
    return this;
  }

  public Iterable<Attribute> toMagmaAttributes(boolean keepOriginalLocalizable) {
    List<Attribute> attrs = Lists.newArrayList();
    for(Map.Entry<String, String> entry : attributes.entrySet()) {

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
      String[] key = entry.getKey().split(":");

      if(key.length > 1) {
        Locale locale = new Locale(key[1]);
        String cleaned = clean(sb.toString());
        Attribute.Builder builder = newAttribute(key[0]).withValue(locale, clean(cleaned));
        attrs.add(builder.build());
        if(keepOriginalLocalizable && cleaned.equals(entry.getValue()) == false) {
          builder = newAttribute("original" + StringUtils.capitalize(key[0])).withValue(locale, entry.getValue());
          attrs.add(builder.build());
        }
      } else {
        Attribute.Builder builder = newAttribute(key[0]).withValue(clean(sb.toString()));
        attrs.add(builder.build());
      }
    }
    return attrs;
  }
  
  private Attribute.Builder newAttribute(String key) {
    Attribute.Builder builder = Attribute.Builder.newAttribute(key);
    if(OPAL_ATTRIBUTES.contains(key) == false) {
      builder.withNamespace(LIMESURVEY_NAMESPACE);
    }
    return builder;
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
