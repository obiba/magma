package org.obiba.magma.datasource.limesurvey;

public class LimesurveyUtils {
  private LimesurveyUtils() {}

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
   * Remove slashes
   *
   * @param name
   * @return
   */
  public static String removeSlashes(String name) {
    return name.replaceAll("/", "");
  }
}
