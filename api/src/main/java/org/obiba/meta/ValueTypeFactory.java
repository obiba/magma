package org.obiba.meta;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.meta.type.BinaryType;
import org.obiba.meta.type.BooleanType;
import org.obiba.meta.type.DateType;
import org.obiba.meta.type.DecimalType;
import org.obiba.meta.type.EnumType;
import org.obiba.meta.type.IntegerType;
import org.obiba.meta.type.LocaleType;
import org.obiba.meta.type.TextType;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ValueTypeFactory {

  private Set<ValueType> types = new HashSet<ValueType>();

  ValueTypeFactory() {
    registerBuiltInTypes();
  }

  public ValueType forClass(final Class<?> javaClass) {
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

  public ValueType forName(final String name) {
    try {
      return Iterables.find(types, new Predicate<ValueType>() {
        @Override
        public boolean apply(ValueType input) {
          return input.getName().equals(name);
        }
      });
    } catch(NoSuchElementException e) {
      throw new IllegalArgumentException("No ValueType named " + name);
    }
  }

  public Set<ValueType> getValueTypes() {
    return Collections.unmodifiableSet(types);
  }

  private void registerBuiltInTypes() {
    types.add(TextType.get());
    types.add(LocaleType.get());
    types.add(DecimalType.get());
    types.add(IntegerType.get());
    types.add(EnumType.get());
    types.add(BooleanType.get());
    types.add(BinaryType.get());
    types.add(DateType.get());
  }

}
