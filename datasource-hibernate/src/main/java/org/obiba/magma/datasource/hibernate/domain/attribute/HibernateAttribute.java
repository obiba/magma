package org.obiba.magma.datasource.hibernate.domain.attribute;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.Attribute;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.datasource.hibernate.domain.ValueHibernateType;

@Entity
@Table(name = "attribute")
@TypeDef(name = "value", typeClass = ValueHibernateType.class)
public class HibernateAttribute extends AbstractEntity implements Attribute {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String name;

  private Locale locale;

  @Type(type = "value")
  @Columns(columns = { @Column(name = "value_type", nullable = false), @Column(name = "is_sequence", nullable = false), @Column(name = "value", length = Integer.MAX_VALUE, nullable = false) })
  private Value value;

  @ManyToOne(optional = false)
  @JoinColumn(name = "attribute_aware_id")
  private AttributeAwareAdapter adapter;

  public HibernateAttribute() {
    super();
  }

  public HibernateAttribute(String name, Locale locale, Value value) {
    super();
    this.name = name;
    this.locale = locale;
    setValue(value);
  }

  public AttributeAwareAdapter getAdapter() {
    return adapter;
  }

  public void setAdapter(AttributeAwareAdapter adapter) {
    this.adapter = adapter;
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
