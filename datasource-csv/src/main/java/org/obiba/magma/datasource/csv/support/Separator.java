package org.obiba.magma.datasource.csv.support;

public enum Separator {
  COMMA(',', ","), SEMICOLON(';', ";"), COLON(':', ":"), TAB('\t', "\t", "\\t", "tab");

  private final char separator;

  private final String[] acceptable;

  Separator(char separator, String... from) {
    this.separator = separator;
    this.acceptable = from;
  }

  public char getCharacter() {
    return separator;
  }

  public static Separator fromString(String value) {
    if(value != null) {
      if(isOneOf(value, SEMICOLON.acceptable)) return SEMICOLON;
      if(isOneOf(value, COLON.acceptable)) return COLON;
      if(isOneOf(value, TAB.acceptable)) return TAB;
    }
    return COMMA;
  }

  private static boolean isOneOf(String value, String[] strings) {
    for(String str : strings) {
      if(value.contains(str)) {
        return true;
      }
    }
    return false;
  }
}
