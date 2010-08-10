package org.obiba.magma.datasource.csv.support;

public enum Separator {
  COMMA(','), SEMICOLON(';'), COLON(':'), TAB('\t');

  private final char separator;

  Separator(char separator) {
    this.separator = separator;
  }

  public char getCharacter() {
    return separator;
  }

  public static Separator fromString(String value) {
    if(value != null && value.length() == 1) {
      if(value.equals(";")) return SEMICOLON;
      else if(value.equals(":")) return COLON;
      else if(value.equals("\t")) return TAB;
    }
    return COMMA;
  }
}
