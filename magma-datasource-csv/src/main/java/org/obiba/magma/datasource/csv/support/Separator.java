package org.obiba.magma.datasource.csv.support;

import java.util.Arrays;

import javax.validation.constraints.NotNull;

import com.google.common.collect.ImmutableList;

public class Separator {

  public static final Separator COMMA = new Separator(',');

  public static final Separator SEMICOLON = new Separator(';');

  public static final Separator COLON = new Separator(':');

  public static final Separator TAB = new Separator('\t', "\\t", "tab");

  private final char separator;

  private final String[] acceptable;

  public Separator(char separator, String... from) {
    this.separator = separator;
    acceptable = from != null ? Arrays.copyOf(from, from.length + 1) : new String[1];
    acceptable[acceptable.length - 1] = String.valueOf(separator);
  }

  public char getCharacter() {
    return separator;
  }

  public static Separator fromString(String value) {
    if(value == null) throw new IllegalArgumentException("value cannot be null");
    for(Separator separator : ImmutableList.of(COMMA, SEMICOLON, COLON, TAB)) {
      if(isOneOf(value, separator.acceptable)) {
        return separator;
      }
    }

    if(value.length() == 1) {
      return new Separator(value.charAt(0));
    }
    throw new IllegalArgumentException("separator must be one character");
  }

  private static boolean isOneOf(@NotNull String value, @NotNull String... strings) {
    for(String str : strings) {
      if(value.contains(str)) {
        return true;
      }
    }
    return false;
  }
}
