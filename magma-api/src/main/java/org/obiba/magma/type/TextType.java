package org.obiba.magma.type;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;

import com.google.common.base.Strings;

import au.com.bytecode.opencsv.CSVParser;

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
  @Nonnull
  public static TextType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new TextType());
    }
    return instance.get();
  }

  @Nonnull
  @Override
  public String getName() {
    return "text";
  }

  @Override
  public Class<?> getJavaClass() {
    return String.class;
  }

  @Override
  public boolean acceptsJavaClass(@Nonnull Class<?> clazz) {
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

  @Nonnull
  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    return Factory.newValue(this, string);
  }

  @Nonnull
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
    String s1 = (String) o1.getValue();
    String s2 = (String) o2.getValue();
    return Strings.nullToEmpty(s1).compareTo(Strings.nullToEmpty(s2));
  }

}
