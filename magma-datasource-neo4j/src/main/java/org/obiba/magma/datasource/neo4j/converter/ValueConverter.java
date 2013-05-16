package org.obiba.magma.datasource.neo4j.converter;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.datasource.neo4j.domain.ValueNode;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class ValueConverter implements Neo4jConverter<ValueNode, Value> {

  private static final ValueConverter INSTANCE = new ValueConverter();

  public static ValueConverter getInstance() {
    return INSTANCE;
  }

  private ValueConverter() {
  }

  @Override
  public ValueNode marshal(Value value, Neo4jMarshallingContext context) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @SuppressWarnings("unchecked")
  @Override
  public Value unmarshal(ValueNode valueNode, Neo4jMarshallingContext context) {
    final ValueType valueType = valueNode.getValueType();
    if(valueNode.isSequence()) {
      Iterable<Value> values = Iterables
          .transform((Iterable<Serializable>) valueNode.getValue(), new Function<Serializable, Value>() {
            @Override
            public Value apply(@Nullable Serializable input) {
              return ValueType.Factory.newValue(valueType, input);
            }
          });
      return ValueType.Factory.newSequence(valueType, values);
    }
    return ValueType.Factory.newValue(valueType, valueNode.getValue());
  }
}
