package org.obiba.magma.type;

import java.util.Locale;

import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

public class LocaleTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return LocaleType.get();
  }

  @Override
  Object getObjectForType() {
    return Locale.CANADA_FRENCH;
  }

  @Override
  boolean isDateTime() {
    return false;
  }

  @Override
  boolean isNumeric() {
    return false;
  }

  @Override
  Iterable<Class<?>> validClasses() {
    return ImmutableList.<Class<?>> of(Locale.class);
  }

}
