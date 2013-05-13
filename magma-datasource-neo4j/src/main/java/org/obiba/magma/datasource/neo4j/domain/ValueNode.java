package org.obiba.magma.datasource.neo4j.domain;

import org.obiba.magma.ValueType;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class ValueNode extends AbstractGraphItem {

  private ValueType valueType;

  private boolean sequence;

  // TODO binaries will be stored with JPA
  // http://api.neo4j.org/1.8/org/neo4j/graphdb/PropertyContainer.html#setProperty(java.lang.String, java.lang.Object)
  private Object value;

  public ValueType getValueType() {
    return valueType;
  }

  public void setValueType(ValueType valueType) {
    this.valueType = valueType;
  }

  public boolean isSequence() {
    return sequence;
  }

  public void setSequence(boolean sequence) {
    this.sequence = sequence;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

}
