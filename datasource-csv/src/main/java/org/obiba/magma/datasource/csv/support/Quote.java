package org.obiba.magma.datasource.csv.support;

public enum Quote {
  DOUBLE('\"'), SINGLE('\'');

  private final char quote;

  Quote(char quote) {
    this.quote = quote;
  }

  public char getCharacter() {
    return quote;
  }

  public static Quote fromString(String value) {
    if(value != null && value.length() == 1) {
      if(value.equals("'")) return SINGLE;
    }
    return DOUBLE;
  }
}
