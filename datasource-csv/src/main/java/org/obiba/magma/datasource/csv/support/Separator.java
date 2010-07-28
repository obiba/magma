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
}
