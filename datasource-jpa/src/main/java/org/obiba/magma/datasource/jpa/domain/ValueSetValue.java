package org.obiba.magma.datasource.jpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

@Entity
@Table(name = "value_set_value")
public class ValueSetValue extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne(optional = false)
  @JoinColumn(name = "value_set_id")
  private ValueSetState valueSet;

  @ManyToOne(optional = false)
  @JoinColumn(name = "variable_id")
  private VariableState variable;

  @Lob
  @Column(length = Integer.MAX_VALUE, nullable = false)
  private String textValue;

  private boolean sequence;

  public ValueSetValue() {

  }

  public ValueSetValue(VariableState variable, ValueSetState valueSet) {
    super();
    if(variable == null) throw new IllegalArgumentException("variable cannot be null");
    if(valueSet == null) throw new IllegalArgumentException("valueSet cannot be null");
    this.variable = variable;
    this.valueSet = valueSet;
  }

  public void setValue(Value value) {
    if(value.isNull()) {
      throw new IllegalArgumentException("cannot persist null values");
    }
    this.textValue = value.toString();
    this.sequence = value.isSequence();
  }

  public Value getValue() {
    ValueType valueType = getVariable().getValueType();
    if(sequence) {
      return valueType.sequenceOf(textValue);
    }
    return valueType.valueOf(textValue);
  }

  public ValueSetState getValueSet() {
    return valueSet;
  }

  public VariableState getVariable() {
    return variable;
  }

}
