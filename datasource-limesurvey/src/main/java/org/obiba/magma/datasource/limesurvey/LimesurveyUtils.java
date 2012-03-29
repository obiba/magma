package org.obiba.magma.datasource.limesurvey;

public class LimesurveyUtils {
  /**
   * Remove ':' and '.'
   *
   * @param name
   * @return
   */
  public static String toValidMagmaName(String name) {
    return name.replaceAll(":", "").replaceAll("\\.", "");
  }

  /**
   * Remove slashes for the moment but magma (and opal) must deal with slashes in name
   *
   * @param name
   * @return
   */
  @Deprecated
  public static String removeSlashes(String name) {
    return name.replaceAll("/", "");
  }
}
