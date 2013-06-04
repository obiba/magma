package org.obiba.magma.datasource.mongodb.converter;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.LocaleType;

import com.mongodb.BasicDBList;

public class ValueConverter {

  public static Object marshall(Variable variable, Value value) {
    if (value == null || value.isNull()) return null;

    if(variable.isRepeatable()) {
      BasicDBList list = new BasicDBList();
      for (Value val : value.asSequence().getValues()) {
        list.add(marshall(val));
      }
      return list;
    } else {
      return marshall(value);
    }
  }

  private static Object marshall(Value value) {
    if (value == null || value.isNull()) return null;
    return value.getValueType().equals(LocaleType.get())  ? value.getValueType().toString(value) : value.getValue();
  }

}
