/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc.support;

import java.text.Normalizer;
import java.util.regex.Pattern;

import liquibase.structure.core.Table;
import liquibase.structure.core.View;

public class TableUtils {

  public static Table newTable(String name) {
    return new Table(null, null, name);
  }

  public static View newView(String name) {
    return new View(null, null, name);
  }

  public static String normalize(String name) {
    return normalize(name, -1);
  }

  public static String normalize(String name, int limit) {
    String normalized = deAccent(name).replaceAll("[^0-9a-zA-Z\\$_]", "");
    return limit > 0 && normalized.length() > limit ? normalized.substring(0, limit - 1) : normalized;
  }

  private static String deAccent(String str) {
    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    return pattern.matcher(nfdNormalizedString).replaceAll("");
  }
}
