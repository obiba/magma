package org.obiba.meta.type;

import java.util.HashSet;
import java.util.Set;

import org.obiba.meta.ValueType;

public class ValueTypeFactory {

  public static final ValueTypeFactory INSTANCE = new ValueTypeFactory();

  private Set<ValueType> types = new HashSet<ValueType>();

  private ValueTypeFactory() {
    registerBuiltInTypes();
  }

  public ValueType forClass(Class<?> javaClass) {
    for(ValueType type : types) {
      if(type.getJavaClass().isAssignableFrom(javaClass)) {
        return type;
      }
    }
    throw new IllegalArgumentException("No ValueType for Java type " + javaClass.getName());
  }

  private void registerBuiltInTypes() {
    types.add(TextType.INSTANCE);
    types.add(ValueSetReferenceType.INSTANCE);
  }

}
