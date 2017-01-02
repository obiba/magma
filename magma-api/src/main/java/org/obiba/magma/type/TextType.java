/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;

import com.google.common.base.Strings;

@SuppressWarnings("MethodReturnAlwaysConstant")
public class TextType extends CSVAwareValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  private static final String QUOTE_STR = "" + QUOTE;

  private static final String ESCAPED_QUOTE_STR = "" + QUOTE + QUOTE;

  @SuppressWarnings("StaticNonFinalField")
  @Nullable
  private static WeakReference<TextType> instance;

  protected TextType() {
  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @NotNull
  public static TextType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new TextType());
    }
    return instance.get();
  }

  @NotNull
  @Override
  public String getName() {
    return "text";
  }

  @Override
  public Class<?> getJavaClass() {
    return String.class;
  }

  @Override
  public boolean acceptsJavaClass(@NotNull Class<?> clazz) {
    return String.class.isAssignableFrom(clazz) || clazz.isEnum();
  }

  @Override
  public boolean isDateTime() {
    return false;
  }

  @Override
  public boolean isNumeric() {
    return false;
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    return Factory.newValue(this, string);
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable Object object) {
    if(object == null) {
      return nullValue();
    }
    return Factory.newValue(this, object.toString());
  }

  /**
   * Adds quotes around the string value and also escapes any quotes in the value by prefixing it with another quote.
   * This is the format expected by the {@code sequenceOf(String string)} method.
   */
  @Nullable
  @Override
  protected String escapeAndQuoteIfRequired(@Nullable String value) {
    String escaped = Strings.nullToEmpty(value);
    // Replace all occurrences of " by ""
    escaped = escaped.replaceAll(QUOTE_STR, ESCAPED_QUOTE_STR);
    return QUOTE + escaped + QUOTE;
  }

  @Override
  public int compare(Value o1, Value o2) {
    if(o1.isNull() && o2.isNull()) return 0;
    if(o1.isNull()) return -1;
    if(o2.isNull()) return 1;

    String s1 = (String) o1.getValue();
    String s2 = (String) o2.getValue();
    return Strings.nullToEmpty(s1).compareTo(Strings.nullToEmpty(s2));
  }

}
