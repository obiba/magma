package org.obiba.magma.datasource.csv.support;

import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.csv.converter.VariableConverter;

public class CsvUtil {

  public static String[] getCsvVariableHeader(ValueTable table) {
    List<String> headers = new ArrayList<String>();

    addVariableHeaders(headers);
    addCategoriesHeaders(table, headers);
    addVariableAttributesHeaders(table, headers);

    String[] headersArray = new String[headers.size()];
    headers.toArray(headersArray);
    return headersArray;
  }

  private static void addVariableHeaders(List<String> headers) {
    headers.add(VariableConverter.NAME);
    headers.add(VariableConverter.VALUE_TYPE);
    headers.add(VariableConverter.ENTITY_TYPE);
    headers.add(VariableConverter.MIME_TYPE);
    headers.add(VariableConverter.UNIT);
    headers.add(VariableConverter.OCCURRENCE_GROUP);
    headers.add(VariableConverter.REPEATABLE);
  }

  private static void addCategoriesHeaders(ValueTable table, List<String> headers) {
    for(Variable variable : table.getVariables()) {
      for(Category category : variable.getCategories()) {
        for(Attribute attribute : category.getAttributes()) {
          if(attribute.getName().equals(VariableConverter.LABEL) && attribute.isLocalised()) {
            String header = VariableConverter.CATEGORIES + ":" + attribute.getLocale();
            if(!headers.contains(header)) {
              headers.add(header);
            }
          }
        }
      }
    }
    if(!headers.get(headers.size() - 1).startsWith(VariableConverter.CATEGORIES)) {
      headers.add(VariableConverter.CATEGORIES);
    }
  }

  private static void addVariableAttributesHeaders(ValueTable table, List<String> headers) {
    for(Variable variable : table.getVariables()) {
      for(Attribute attribute : variable.getAttributes()) {
        String header = attribute.getName();
        if(attribute.isLocalised()) {
          header += ":" + attribute.getLocale();
        }
        if(!headers.contains(header)) {
          headers.add(header);
        }
      }
    }
  }

}
