package org.obiba.magma.datasource.limesurvey;

import java.util.Arrays;
import java.util.List;

public enum LimesurveyImplicitType {
  A("1", "2", "3", "4", "5"), //
  B("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), //
  C("Y", "N"), //
  E("I", "S", "D"), //
  G("M", "F"), //
  Y("Y", "N"), //
  M("Y", "N"), //
  P("Y", "N"), //
  _5("1", "2", "3", "4", "5");

  private List<String> categories;

  LimesurveyImplicitType(String label, String... categories) {
    this.categories = Arrays.asList(categories);
  }

  public List<String> getCategories() {
    return categories;
  }

  public static LimesurveyImplicitType _valueOf(String value) {
    if(value.equals("5")) {
      return valueOf("_5");
    }
    return valueOf(value);
  }

}
