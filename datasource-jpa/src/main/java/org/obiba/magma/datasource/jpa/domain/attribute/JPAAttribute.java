package org.obiba.magma.datasource.jpa.domain.attribute;

import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.Attribute;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.datasource.jpa.domain.ValueTypeType;

@Entity
@Table(name = "attribute")
@TypeDef(name = "value_type", typeClass = ValueTypeType.class)
public class JPAAttribute extends AbstractEntity implements Attribute {

  private static final long serialVersionUID = 1L;

  private String name;

  private Locale locale;

  private String textValue;

  @Type(type = "value_type")
  private ValueType valueType;

  @ManyToOne(optional = false)
  @JoinColumn(name = "attribute_aware_id")
  private AttributeAwareAdapter adapter;

  public JPAAttribute() {
    super();
  }

  public JPAAttribute(String name, Locale locale, Value value) {
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
    if(value != null) {
      this.valueType = value.getValueType();
      this.textValue = valueType.toString(value);
    } else {
      this.valueType = null;
      this.textValue = null;
    }
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
    return valueType.valueOf(textValue);
  }

  @Override
  public ValueType getValueType() {
    return valueType;
  }

  @Override
  public boolean isLocalised() {
    return locale != null;
  }

}
