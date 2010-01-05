package org.obiba.magma.datasource.hibernate.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.Value;

@Entity
@Table(name = "value_set_value", uniqueConstraints = { @UniqueConstraint(columnNames = { "value_set_id", "variable_id" }) })
@TypeDef(name = "value", typeClass = ValueHibernateType.class)
public class ValueSetValue extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne(optional = false)
  @JoinColumn(name = "value_set_id")
  private ValueSetState valueSet;

  @ManyToOne(optional = false)
  @JoinColumn(name = "variable_id")
  private VariableState variable;

  @Type(type = "value")
  @Columns(columns = { @Column(name = "value_type", nullable = false), @Column(name = "is_sequence", nullable = false), @Column(name = "value", length = Integer.MAX_VALUE, nullable = false) })
  private Value value;

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
    this.value = value;
  }

  public Value getValue() {
    return value;
  }

  public ValueSetState getValueSet() {
    return valueSet;
  }

  public VariableState getVariable() {
    return variable;
  }

}
