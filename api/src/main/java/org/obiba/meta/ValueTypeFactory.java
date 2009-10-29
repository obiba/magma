package org.obiba.meta;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.obiba.meta.type.BinaryType;
import org.obiba.meta.type.DateType;
import org.obiba.meta.type.DecimalType;
import org.obiba.meta.type.EnumType;
import org.obiba.meta.type.IntegerType;
import org.obiba.meta.type.TextType;

public class ValueTypeFactory {

  private Set<ValueType> types = new HashSet<ValueType>();

  ValueTypeFactory() {
    registerBuiltInTypes();
  }

  public ValueType forClass(Class<?> javaClass) {
    for(ValueType type : types) {
      if(type.acceptsJavaClass(javaClass)) {
        return type;
      }
    }
    throw new IllegalArgumentException("No ValueType for Java type " + javaClass.getName());
  }

  public Set<ValueType> getValueTypes() {
    return Collections.unmodifiableSet(types);
  }

  private void registerBuiltInTypes() {
    types.add(TextType.get());
    types.add(DecimalType.get());
    types.add(IntegerType.get());
    types.add(EnumType.get());
    types.add(BinaryType.get());
    types.add(DateType.get());
  }

}
