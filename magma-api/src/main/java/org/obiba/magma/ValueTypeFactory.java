package org.obiba.magma;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.type.AnyToTextValueConverter;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DatetimeValueConverter;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IdentityValueConverter;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.NumericValueConverter;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
import org.obiba.magma.type.TextToAnyTypeValueConverter;
import org.obiba.magma.type.TextToNumericTypeValueConverter;
import org.obiba.magma.type.TextType;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

class ValueTypeFactory {

  private final Set<ValueType> types = new HashSet<>();

  private final Collection<ValueConverter> converters = new LinkedHashSet<>();

  ValueTypeFactory() {
    registerBuiltInTypes();
  }

  ValueType forClass(final Class<?> javaClass) {
    try {
      return Iterables.find(types, new Predicate<ValueType>() {
        @Override
        public boolean apply(ValueType input) {
          return input.acceptsJavaClass(javaClass);
        }
      });
    } catch(NoSuchElementException e) {
      throw new IllegalArgumentException("No ValueType for Java type " + javaClass.getName());
    }
  }

  ValueType forName(@Nullable final String name) {
    try {
      return Iterables.find(types, new Predicate<ValueType>() {
        @Override
        public boolean apply(ValueType input) {
          return input.getName().equalsIgnoreCase(name);
        }
      });
    } catch(NoSuchElementException e) {
      throw new IllegalArgumentException("No ValueType named " + name);
    }
  }

  Set<ValueType> getValueTypes() {
    return Collections.unmodifiableSet(types);
  }

  @NotNull
  ValueConverter converterFor(final ValueType from, final ValueType to) {
    try {
      return Iterables.find(converters, new Predicate<ValueConverter>() {

        @Override
        public boolean apply(ValueConverter input) {
          return input.converts(from, to);
        }
      });
    } catch(NoSuchElementException e) {
      throw new IllegalArgumentException("No ValueConverter for " + from.getName() + "->" + to.getName());
    }
  }

  private void registerBuiltInTypes() {
    types.add(TextType.get());
    types.add(LocaleType.get());
    types.add(DecimalType.get());
    types.add(IntegerType.get());
    types.add(BooleanType.get());
    types.add(BinaryType.get());
    types.add(DateTimeType.get());
    types.add(DateType.get());
    types.add(PointType.get());
    types.add(LineStringType.get());
    types.add(PolygonType.get());

    converters.add(new IdentityValueConverter());
    converters.add(new TextToNumericTypeValueConverter());
    converters.add(new DatetimeValueConverter());
    converters.add(new NumericValueConverter());
    converters.add(new AnyToTextValueConverter());
    converters.add(new TextToAnyTypeValueConverter());
  }

}
