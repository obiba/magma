/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
