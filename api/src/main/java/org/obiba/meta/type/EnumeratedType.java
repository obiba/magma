package org.obiba.meta.type;

import org.obiba.meta.ValueType;

public interface EnumeratedType extends ValueType {

  public String[] enumerate(Class<?> enumClass);

}
