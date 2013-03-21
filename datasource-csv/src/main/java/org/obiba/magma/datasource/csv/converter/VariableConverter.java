package org.obiba.magma.datasource.csv.converter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.obiba.magma.Attribute;
import org.obiba.magma.Attributes;
import org.obiba.magma.Category;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.csv.support.CsvDatasourceParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class VariableConverter {

  private static final Logger log = LoggerFactory.getLogger(VariableConverter.class);

  public static final String NAME = "name";

  public static final String VALUE_TYPE = "valueType";

  public static final String ENTITY_TYPE = "entityType";

  public static final String REFERENCED_ENTITY_TYPE = "referencedEntityType";

  public static final String MIME_TYPE = "mimeType";

  public static final String REPEATABLE = "repeatable";

  public static final String OCCURRENCE_GROUP = "occurrenceGroup";

  public static final String UNIT = "unit";

  public static final String CATEGORIES = "categories";

  public static final String LABEL = "label";

  public static final List<String> reservedVariableHeaders = Lists.newArrayList(NAME, //
      VALUE_TYPE, //
      ENTITY_TYPE, //
      REFERENCED_ENTITY_TYPE, //
      MIME_TYPE, //
      UNIT, //
      REPEATABLE, //
      OCCURRENCE_GROUP);

  public static final List<String> categoriesReservedAttributeNames = Lists
      .newArrayList("table", "variable", "name", "code", "missing");

  private final Map<String, Integer> headerMap = new HashMap<String, Integer>();

  private final String[] header;

  public VariableConverter(String... headers) {
    header = new String[headers.length];
    for(int i = 0; i < headers.length; i++) {
      String headerColumnName = headers[i].trim();
      headerMap.put(headerColumnName, i);
      header[i] = headerColumnName;
    }
    if(log.isTraceEnabled()) {
      for(Entry<String, Integer> entry : headerMap.entrySet()) {
        log.trace("headerMap[{}]={}", entry.getKey(), entry.getValue());
      }
    }
    validateHeader();
  }

  private void validateHeader() {
    if(!headerMap.containsKey(NAME))
      throw new CsvDatasourceParsingException("The variables.csv header must contain 'name'.",
          "CsvVariablesHeaderMustContainName", 0);
    if(!headerMap.containsKey(VALUE_TYPE))
      throw new CsvDatasourceParsingException("The variables.csv header must contain 'valueType'.",
          "CsvVariablesHeaderMustContainValueType", 0);
    if(!headerMap.containsKey(ENTITY_TYPE))
      throw new CsvDatasourceParsingException("The variables.csv header must contain 'entityType'.",
          "CsvVariablesHeaderMustContainEntityType", 0);
  }

  public Variable unmarshal(String... csvVar) {
    String name = getValueAt(csvVar, NAME);
    String valueType = getValueAt(csvVar, VALUE_TYPE);
    String entityType = getValueAt(csvVar, ENTITY_TYPE);
    String referencedEntityType = getValueAt(csvVar, REFERENCED_ENTITY_TYPE);
    String mimeType = getValueAt(csvVar, MIME_TYPE);
    String unit = getValueAt(csvVar, UNIT);
    String repeatable = getValueAt(csvVar, REPEATABLE);
    String occurrenceGroup = getValueAt(csvVar, OCCURRENCE_GROUP);

    log.debug("name={} valueType={} entityType={} mimeType={}", name, valueType, entityType, mimeType);

    Variable.Builder builder = Variable.Builder.newVariable(name, ValueType.Factory.forName(valueType), entityType)
        .mimeType(mimeType).unit(unit).occurrenceGroup(occurrenceGroup).referencedEntityType(referencedEntityType);

    if(Boolean.parseBoolean(repeatable)) {
      builder.repeatable();
    }

    // attributes and categories
    Map<String, Category.Builder> categoryBuilderMap = new LinkedHashMap<String, Category.Builder>();
    for(String header : headerMap.keySet()) {
      if(!reservedVariableHeaders.contains(header)) {
        String value = getValueAt(csvVar, header);
        if(value != null) {
          if(header.startsWith(CATEGORIES)) {
            unmarshalCategories(value, getAttributeLocale(header), categoryBuilderMap);
          } else {
            Attribute.Builder attrBuilder = Attributes.decodeFromHeader(header);
            builder.addAttribute(attrBuilder.withValue(value).build());
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
    for(String cat1 : cats) {
      String[] cat = cat1.trim().split("=");

      String catName = cat[0].trim();
      Category.Builder catBuilder;
      if(categoryBuilderMap.containsKey(catName)) {
        catBuilder = categoryBuilderMap.get(catName);
      } else {
        catBuilder = Category.Builder.newCategory(catName);
        categoryBuilderMap.put(catName, catBuilder);
      }
      if(cat.length > 1) catBuilder.addAttribute(LABEL, cat[1].trim(), locale);
    }
  }

  private String marshalCategories(Variable variable, Locale locale) {
    StringBuilder sb = new StringBuilder();
    for(Category category : variable.getCategories()) {
      sb.append(category.getName());
      Attribute label = null;
      if(category.hasAttribute(LABEL)) {
        label = locale != null && category.hasAttribute(LABEL, locale)
            ? category.getAttribute(LABEL, locale)
            : category.getAttribute(LABEL);
      }
      if(label != null) {
        sb.append("=").append(label.getValue().toString());
      }
      sb.append(";"); // TODO configure separator.
    }
    if(sb.length() > 0) sb.setLength(sb.length() - 1); // Remove last separator.
    return sb.toString();
  }

  @Nullable
  private Locale getAttributeLocale(String header) {
    String[] h = header.split(":");
    if(h.length > 1 && !h[1].trim().isEmpty()) {
      return new Locale(h[1].trim());
    }
    return null;
  }

  @Nullable
  private String getValueAt(String[] csvVar, String header) {
    String value = null;
    Integer pos = headerMap.get(header);
    if(pos != null && pos < csvVar.length) {
      value = csvVar[pos];
      if(value.isEmpty()) {
        value = null;
      }
    }
    return value;
  }

  public String[] marshal(Variable variable) {
    Map<Integer, String> resultMap = new HashMap<Integer, String>();

    resultMap.put(headerMap.get(NAME), variable.getName());
    resultMap.put(headerMap.get(VALUE_TYPE), variable.getValueType().getName());
    resultMap.put(headerMap.get(ENTITY_TYPE), variable.getEntityType());
    if(headerMap.containsKey(MIME_TYPE)) resultMap.put(headerMap.get(MIME_TYPE), variable.getMimeType());
    if(headerMap.containsKey(REPEATABLE))
      resultMap.put(headerMap.get(REPEATABLE), Boolean.toString(variable.isRepeatable()));
    if(headerMap.containsKey(OCCURRENCE_GROUP))
      resultMap.put(headerMap.get(OCCURRENCE_GROUP), variable.getOccurrenceGroup());
    if(headerMap.containsKey(UNIT)) resultMap.put(headerMap.get(UNIT), variable.getUnit());
    if(headerMap.containsKey(REFERENCED_ENTITY_TYPE))
      resultMap.put(headerMap.get(REFERENCED_ENTITY_TYPE), variable.getReferencedEntityType());

    for(Attribute attribute : variable.getAttributes()) {
      String header = Attributes.encodeForHeader(attribute);
      if(headerMap.containsKey(header) && !reservedVariableHeaders.contains(header)) {
        resultMap.put(headerMap.get(header), attribute.getValue().toString());
      }
    }

    for(String header : headerMap.keySet()) {
      if(header.startsWith(CATEGORIES)) {
        resultMap.put(headerMap.get(header), marshalCategories(variable, getAttributeLocale(header)));
      }
    }

    String[] result = new String[headerMap.size()];
    for(int i = 0; i < headerMap.size(); i++) {
      if(resultMap.containsKey(i)) {
        result[i] = resultMap.get(i);
      }
    }

    return result;
  }

  public String[] getHeader() {
    return Arrays.copyOf(header, header.length);
  }
}
