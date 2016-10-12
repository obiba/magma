/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.type;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;

import au.com.bytecode.opencsv.CSVParser;

public abstract class JSONAwareValueType extends AbstractValueType {

  private static final long serialVersionUID = 3530785145782662091L;

  private static final char DEL_CHAR = (char) 127;

  private transient CSVParser csvParser;

  /**
   * Reads a JSON string value of strings. The format of the string is
   * <p/>
   * <pre>
   * "value","value","value"
   * </pre>
   * <p/>
   * When the original {@code value} contains a {@code "}, it is escaped by adding another {@code "}, as per the CSV
   * standard.
   */
  @NotNull
  @Override
  public ValueSequence sequenceOf(@Nullable String string) {
    if(string == null) {
      return nullSequence();
    }
    Collection<Value> values = new ArrayList<>();

    // Special case for empty string
    if(string.isEmpty()) {
      values.add(valueOf(string));
      return sequenceOf(values);
    }

    try {
      JSONArray array = new JSONArray(string);
      for(int i = 0; i < array.length(); i++) {
        values.add(valueOf(array.get(i)));
      }
    } catch(JSONException e) {
      throw new MagmaRuntimeException("Invalid value sequence formatting: " + string, e);
    }

    return sequenceOf(values);
  }

}
