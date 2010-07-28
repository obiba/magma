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
}
