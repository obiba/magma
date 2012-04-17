package org.obiba.magma.datasource.hibernate.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Locale;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Parent;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.obiba.magma.Attribute;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.hibernate.type.ValueHibernateType;

@Embeddable
@TypeDef(name = "value", typeClass = ValueHibernateType.class)
public class AttributeState implements Attribute {

  private static final long serialVersionUID = 1L;

  @Parent
  private AbstractAttributeAwareEntity parent;

  @Column(nullable = false)
  private String name;

  private Locale locale;

  @Type(type = "value")
  @Columns(columns = {@Column(name = "value_type", nullable = false), @Column(name = "is_sequence",
      nullable = false), @Column(name = "value", length = Integer.MAX_VALUE, nullable = false)})
  private Value value;

  public AttributeState() {
    super();
  }

  public AttributeState(String name, Locale locale, Value value) {
    super();
    this.name = name;
    this.locale = locale;
    setValue(value);
  }

  public AbstractAttributeAwareEntity getParent() {
    return parent;
  }

  public void setParent(AbstractAttributeAwareEntity parent) {
    this.parent = parent;
  }

  public void setValue(Value value) {
    this.value = value;
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Value getValue() {
    return value;
  }

  @Override
  public ValueType getValueType() {
    return value.getValueType();
  }

  @Override
  public boolean isLocalised() {
    return locale != null;
  }

}
