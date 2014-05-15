package org.obiba.magma.datasource.csv.support;

import com.google.common.collect.ImmutableList;

public class Quote {

  public static final Quote DOUBLE = new Quote('"');

  public static final Quote SINGLE = new Quote('\'');

  private final char quote;

  public Quote(char quote) {
    this.quote = quote;
  }

  public char getCharacter() {
    return quote;
  }

  public static Quote fromString(@SuppressWarnings("TypeMayBeWeakened") String value) {
    if(value == null) throw new IllegalArgumentException("value cannot be null");
    for(Quote quote : ImmutableList.of(DOUBLE, SINGLE)) {
      if(value.equals("" + quote.quote)) {
        return quote;
      }
    }

    if(value.length() == 1) {
      return new Quote(value.charAt(0));
    } else {
      throw new IllegalArgumentException("quote must be one character");
    }
  }
}
