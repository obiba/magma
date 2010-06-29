package org.obiba.magma.datasource.csv.converter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.obiba.magma.Category;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class VariableConverter {

  private static final Logger log = LoggerFactory.getLogger(VariableConverter.class);

  public static final String NAME = "name";

  public static final String VALUE_TYPE = "valueType";

  public static final String ENTITY_TYPE = "entityType";

  public static final String MIME_TYPE = "mimeType";

  public static final String REPEATABLE = "repeatable";

  public static final String OCCURRENCE_GROUP = "occurrenceGroup";

  public static final String UNIT = "unit";

  public static final String CATEGORIES = "categories";

  public static final List<String> reservedVariableHeaders = Lists.newArrayList(NAME, //
  VALUE_TYPE, //
  ENTITY_TYPE, //
  MIME_TYPE, //
  UNIT, //
  REPEATABLE, //
  OCCURRENCE_GROUP);

  public static final List<String> categoriesReservedAttributeNames = Lists.newArrayList("table", "variable", "name", "code", "missing");

  private Map<String, Integer> headerMap = new HashMap<String, Integer>();

  public VariableConverter(String[] headers) {
    super();
    for(int i = 0; i < headers.length; i++) {
      headerMap.put(headers[i].trim(), i);
    }
    for(Entry<String, Integer> entry : headerMap.entrySet()) {
      log.debug("headerMap[{}]={}", entry.getKey(), entry.getValue());
    }
  }

  public Variable unmarshal(String[] csvVar) {
    String name = getValueAt(csvVar, NAME);
    String valueType = getValueAt(csvVar, VALUE_TYPE);
    String entityType = getValueAt(csvVar, ENTITY_TYPE);
    String mimeType = getValueAt(csvVar, MIME_TYPE);
    String unit = getValueAt(csvVar, UNIT);
    String repeatable = getValueAt(csvVar, REPEATABLE);
    String occurrenceGroup = getValueAt(csvVar, OCCURRENCE_GROUP);

    Variable.Builder builder = Variable.Builder.newVariable(name, ValueType.Factory.forName(valueType), entityType).mimeType(mimeType).unit(unit).occurrenceGroup(occurrenceGroup);

    if(Boolean.parseBoolean(repeatable)) {
      builder.repeatable();
    }

    // attributes and categories
    Map<String, Category.Builder> categoryBuilderMap = new LinkedHashMap<String, Category.Builder>();
    for(String header : headerMap.keySet()) {
      if(!reservedVariableHeaders.contains(header)) {
        String value = getValueAt(csvVar, header);
        if(value != null) {
          String attName = getAttributeName(header);
          if(attName.equals(CATEGORIES)) {
            unmarshalCategories(value, getAttributeLocale(header), categoryBuilderMap);
          } else {
            builder.addAttribute(attName, value, getAttributeLocale(header));
          }
        }
      }
    }

    for(Map.Entry<String, Category.Builder> entry : categoryBuilderMap.entrySet()) {
      builder.addCategory(entry.getValue().build());
    }

    return builder.build();
  }

  private void unmarshalCategories(String categories, Locale locale, Map<String, Category.Builder> categoryBuilderMap) {
    String[] cats = categories.split(";");
    for(int i = 0; i < cats.length; i++) {
      String[] cat = cats[i].trim().split("=");

      String catName = cat[0].trim();
      Category.Builder catBuilder;
      if(categoryBuilderMap.containsKey(catName)) {
        catBuilder = categoryBuilderMap.get(catName);
      } else {
        catBuilder = Category.Builder.newCategory(catName);
        categoryBuilderMap.put(catName, catBuilder);
      }
      if(cat.length > 1) catBuilder.addAttribute("label", cat[1].trim(), locale);
    }
  }

  private String getAttributeName(String header) {
    return header.split(":")[0];
  }

  private Locale getAttributeLocale(String header) {
    String[] h = header.split(":");
    if(h.length > 1) return new Locale(h[1]);
    return null;
  }

  private String getValueAt(String[] csvVar, String header) {
    String value = null;
    Integer pos = headerMap.get(header);
    if(pos != null && pos < csvVar.length) {
      value = csvVar[pos];
      if(value.length() == 0) {
        value = null;
      }
    }
    return value;
  }

}
