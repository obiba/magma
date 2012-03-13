package org.obiba.magma.datasource.limesurvey;

import java.util.Arrays;
import java.util.List;

public enum LimesurveyCategoryGroupType {

  IMPLICIT("A", "B", "C", "E", "G", "Y", "M", "P", "5"), //
  EXPLICIT("H", "1", "R", "!", "L", "O"), //
  EXPLICIT_OPTION_OTH("!", "L", "O");

  private List<String> types;

  LimesurveyCategoryGroupType(String... types) {
    this.types = Arrays.asList(types);
  }

  public static boolean isImplicitCategory(String type) {
    return IMPLICIT.types.contains(type);
  }

  public static boolean isExplicitCategory(String type) {
    return EXPLICIT.types.contains(type);
  }

  public static boolean isExplicitOptionOthCategory(String type) {
    return EXPLICIT_OPTION_OTH.types.contains(type);
  }

}
