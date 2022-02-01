/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.type;

import au.com.bytecode.opencsv.CSVParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class FormattedValueType extends AbstractValueType {

  private static final long serialVersionUID = 1864782499603380247L;

  private static final char DEL_CHAR = (char) 127;

  private transient CSVParser csvParser;

  /**
   * Reads a comma-separated string value of strings. The format of the string is
   * <p/>
   * <pre>
   * "value","value","value"
   * </pre>
   * <p/>
   * When the original {@code value} contains a {@code "}, it is escaped by adding another {@code "}, as per the CSV
   * standard.
   *
   * Reads JSON array format when input string is enclosed with brackets.
   */
  @NotNull
  @Override
  public ValueSequence sequenceOf(@Nullable String string) {
    if (string == null) {
      return nullSequence();
    }
    Collection<Value> values = new ArrayList<>();
    if (string.startsWith("[") && string.endsWith("]")) {
      // expect JSON format
      JSONArray array = new JSONArray(string);
      for (int i = 0; i < array.length(); i++) {
        Object val = array.get(i);
        values.add(valueOf(val == JSONObject.NULL ? null : val));
      }
    } else {
      try {
        for (String currentValue : getCsvParser().parseLine(string)) {
          values.add(valueOf(currentValue.isEmpty() ? null : currentValue));
        }
      } catch (IOException e) {
        throw new MagmaRuntimeException("Invalid value sequence formatting: " + string, e);
      }
    }

    return sequenceOf(values);
  }

  @Nullable
  @Override
  protected String escapeAndQuoteIfRequired(@Nullable String value) {
    return QUOTE + value + QUOTE;
  }

  private CSVParser getCsvParser() {
    if (csvParser == null) {
      // we don't want escape processing try DEL as a rare character until we can turn it off
      csvParser = new CSVParser(SEPARATOR, QUOTE, DEL_CHAR);
    }
    return csvParser;
  }

}
